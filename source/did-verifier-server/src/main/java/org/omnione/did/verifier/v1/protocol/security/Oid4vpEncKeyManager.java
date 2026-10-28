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

package org.omnione.did.verifier.v1.protocol.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.oid4vc.oid4vp.exception.OID4VPException;
import org.omnione.did.oid4vc.oid4vp.service.VerifierConfigService;
import org.omnione.did.oid4vc.oid4vp.util.crypto.JweResponseDecryptor;
import org.omnione.did.oid4vc.oid4vp.util.crypto.VPTokenEncryptor;
import org.springframework.stereotype.Component;

import java.security.interfaces.ECPrivateKey;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-transaction ephemeral EC P-256 key pairs for OID4VP JWE response encryption.
 * Decryption itself is encapsulated by the SDK ({@link JweResponseDecryptor}); this class only handles keys and routing.
 * alg/enc are not hard-coded but read from {@link VerifierConfigService} (admin DB settings, {@code oid4vp_config}),
 * the same source as the clientMetadata/crypto settings.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class Oid4vpEncKeyManager {

    private final ObjectMapper objectMapper;
    private final JweResponseDecryptor jweResponseDecryptor;
    private final VerifierConfigService verifierConfigService;
    private final VPTokenEncryptor vpTokenEncryptor;

    public ECKey generateEphemeralKeyPair() {
        try {
            String alg = verifierConfigService.getOID4VPConfig().getEncryption().getAlg();
            return new ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.ENCRYPTION)
                    .algorithm(new Algorithm(alg))
                    .keyID(UUID.randomUUID().toString())
                    .generate();
        } catch (JOSEException e) {
            log.error("Failed to generate ephemeral EC key pair", e);
            throw new OpenDidException(ErrorCode.OID4VP_ENC_KEY_GENERATION_FAILED, e);
        }
    }

    /** Builds the jwks and enc metadata (public key only) for client_metadata, passed as the clientMetadata argument of InitiationService.initiateVerification. */
    public String buildClientMetadataJson(ECKey ephemeralKeyPair) {
        try {
            String enc = verifierConfigService.getOID4VPConfig().getEncryption().getEnc();

            Map<String, Object> jwks = new LinkedHashMap<>();
            jwks.put("keys", List.of(ephemeralKeyPair.toPublicJWK().toJSONObject()));

            Map<String, Object> clientMetadata = new LinkedHashMap<>();
            clientMetadata.put("jwks", jwks);
            clientMetadata.put("encrypted_response_enc_values_supported", List.of(enc));

            return objectMapper.writeValueAsString(clientMetadata);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize client_metadata jwks", e);
            throw new OpenDidException(ErrorCode.OID4VP_ENC_KEY_GENERATION_FAILED, e);
        }
    }

    /**
     * Encrypts the full private JWK JSON (including d) with {@link VPTokenEncryptor} (AES-256-GCM) into a storable form.
     * The result is stored in Oid4vpSession.encPrivateKeyJwk, with the same at-rest protection as vp_token in the session table.
     */
    public String toStorableJwk(ECKey ephemeralKeyPair) {
        return vpTokenEncryptor.encrypt(ephemeralKeyPair.toJSONString());
    }

    public String extractKid(String jweCompact) {
        String kid;
        try {
            kid = jweResponseDecryptor.parseHeader(jweCompact).getKeyID();
        } catch (OID4VPException e) {
            log.warn("Failed to extract kid from JWE header: {}", e.getErrorMsg());
            throw new OpenDidException(ErrorCode.OID4VP_JWE_HEADER_PARSE_FAILED, e);
        } catch (RuntimeException e) {
            // For a corrupted header (e.g. a missing enc field), Nimbus JWEHeader.parse() throws a raw exception such as
            // NullPointerException instead of OID4VPException (from EncryptionMethod.parse). If it is not caught here,
            // it escapes the controller without being handled by GlobalControllerAdvice, is forwarded internally to the
            // GET-only error page (SpaController), and is masked as an unexplained 405.
            log.warn("Failed to extract kid from JWE header (unexpected {}): {}",
                    e.getClass().getSimpleName(), e.getMessage());
            throw new OpenDidException(ErrorCode.OID4VP_JWE_HEADER_PARSE_FAILED, e);
        }
        if (kid == null || kid.isBlank()) {
            log.warn("JWE header has no kid claim");
            throw new OpenDidException(ErrorCode.OID4VP_JWE_HEADER_PARSE_FAILED);
        }
        return kid;
    }

    public ECPrivateKey loadPrivateKey(String storedJwk) {
        if (storedJwk == null || storedJwk.isBlank()) {
            log.error("Stored enc private key JWK is null or blank");
            throw new OpenDidException(ErrorCode.OID4VP_ENC_KEY_LOAD_FAILED);
        }
        try {
            String decryptedJwk = vpTokenEncryptor.decrypt(storedJwk);
            return ECKey.parse(decryptedJwk).toECPrivateKey();
        } catch (ParseException | JOSEException | RuntimeException e) {
            log.error("Failed to load stored enc private key JWK", e);
            throw new OpenDidException(ErrorCode.OID4VP_ENC_KEY_LOAD_FAILED, e);
        }
    }
}
