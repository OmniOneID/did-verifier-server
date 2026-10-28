/*
 * Copyright 2026 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnione.did.verifier.v1.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.Oid4vpConfig;
import org.omnione.did.base.db.repository.Oid4vpConfigRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.oid4vc.oid4vp.config.OID4VPConfig;
import org.omnione.did.oid4vc.oid4vp.service.VerifierConfigService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class Oid4vpConfigService {

    private static final String CONFIG_TYPE = "OID4VP";

    private final Oid4vpConfigRepository oid4vpConfigRepository;
    private final VerifierConfigService verifierConfigService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getConfig() {
        Oid4vpConfig entity = oid4vpConfigRepository.findByType(CONFIG_TYPE)
                .orElse(null);

        if (entity == null) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(entity.getConfig(), Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse OID4VP config JSON", e);
            throw new OpenDidException(ErrorCode.FAILED_TO_INITIATE_VERIFICATION);
        }
    }

    public void saveConfig(Map<String, Object> configMap) {
        try {
            String configJson = objectMapper.writeValueAsString(configMap);

            // validate by parsing as OID4VPConfig
            OID4VPConfig parsedConfig = objectMapper.readValue(configJson, OID4VPConfig.class);
            validateEncryptionAlgorithm(parsedConfig);
            validateResponseMode(configMap);

            Oid4vpConfig entity = oid4vpConfigRepository.findByType(CONFIG_TYPE)
                    .orElse(Oid4vpConfig.builder().type(CONFIG_TYPE).build());
            entity.setConfig(configJson);
            oid4vpConfigRepository.save(entity);

            // reload SDK cache
            verifierConfigService.reloadConfig();
            log.info("OID4VP config saved and SDK cache reloaded");
        } catch (JsonProcessingException e) {
            log.error("Invalid OID4VP config JSON", e);
            throw new OpenDidException(ErrorCode.FAILED_TO_INITIATE_VERIFICATION, e);
        }
    }

    /**
     * The SDK JweResponseDecryptor enforces a hardcoded ECDH-ES/A256GCM and does not read this config
     * (an intentional security decision: no algorithm negotiation, ECDH-ES/A256GCM is fixed for wallets).
     * The encryption.alg/enc in this config only advertise that fixed value to the wallet, so saving another value
     * would advertise it to the wallet while actual decryption always fails, a silent mismatch.
     * Rejecting it explicitly at save time prevents that drift.
     */
    private void validateEncryptionAlgorithm(OID4VPConfig config) {
        OID4VPConfig.Encryption encryption = config.getEncryption();
        String alg = encryption != null ? encryption.getAlg() : null;
        String enc = encryption != null ? encryption.getEnc() : null;

        if (!JWEAlgorithm.ECDH_ES.getName().equals(alg) || !EncryptionMethod.A256GCM.getName().equals(enc)) {
            log.warn("Rejected OID4VP config save: unsupported encryption alg/enc (alg={}, enc={})", alg, enc);
            throw new OpenDidException(ErrorCode.OID4VP_ENCRYPTION_ALGORITHM_NOT_SUPPORTED);
        }
    }

    /**
     * responseMode has no field in OID4VPConfig (SDK), so it is read directly from the raw configMap.
     * Only direct_post.jwt is supported, so the admin screen shows it as read-only. Because
     * Oid4vpProtocolHandler reads this value and uses it for initiate, saving another value by calling
     * the API directly would silently disable JWE enforcement unless it is rejected here.
     */
    private void validateResponseMode(Map<String, Object> configMap) {
        Object responseMode = configMap.get("responseMode");
        if (responseMode != null && !"direct_post.jwt".equals(responseMode.toString())) {
            log.warn("Rejected OID4VP config save: unsupported responseMode ({})", responseMode);
            throw new OpenDidException(ErrorCode.OID4VP_RESPONSE_MODE_NOT_SUPPORTED);
        }
    }
}
