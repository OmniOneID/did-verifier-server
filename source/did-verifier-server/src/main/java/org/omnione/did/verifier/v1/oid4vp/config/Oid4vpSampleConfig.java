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

package org.omnione.did.verifier.v1.oid4vp.config;

import org.omnione.did.oid4vc.oid4vp.dto.OID4VPConfigDto;
import org.omnione.did.oid4vc.oid4vp.repository.OID4VPRepository;
import org.omnione.did.oid4vc.oid4vp.repository.impl.InMemoryOID4VPRepository;
import org.omnione.did.oid4vc.oid4vp.util.crypto.VPTokenEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * OID4VP configuration for the sample profile, which runs without a database.
 * The OID4VP SDK reads its configuration at startup, so an in-memory repository
 * holding a default configuration is provided instead of the JPA adapter.
 */
@Configuration
@Profile("sample")
public class Oid4vpSampleConfig {

    private static final String SAMPLE_CONFIG = """
            {"baseUrl":"http://127.0.0.1:8092","clientName":"OpenDID Verifier","invocationScheme":"openid4vp://",\
            "clientId":{"scheme":"decentralized_identifier","value":"did:omn:verifier"},\
            "session":{"sessionTtl":300000},\
            "endpoints":{"response":"/oid4vp/response","request":"/oid4vp/request"},\
            "clientMetadata":{"vpFormatsSupported":{"mso_mdoc-did":{"alg_values":["ES256"]},"dc+sd-jwt-did":{},"vc+sd-jwt":{},"opendid_vc":{}}},\
            "crypto":{"vpTokenEncryptionKey":"%s"},\
            "encryption":{"alg":"ECDH-ES","enc":"A256GCM"},\
            "verification":{"skipX5cChainValidation":false,"enforceClaimConstraints":false}}""";

    @Bean
    @Primary
    public OID4VPRepository sampleOid4vpRepository() {
        InMemoryOID4VPRepository repository = new InMemoryOID4VPRepository();
        repository.save(OID4VPConfigDto.builder()
                .type("OID4VP")
                .config(SAMPLE_CONFIG.formatted(VPTokenEncryptor.generateKey()))
                .build());
        return repository;
    }
}
