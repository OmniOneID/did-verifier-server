/*
 * Copyright 2025 OmniOne.
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

package org.omnione.did.verifier.v1.agent.config;

import org.omnione.did.verifier.v1.config.VerifierSdkBuilder;
import org.omnione.did.verifier.v1.provider.*;
import org.omnione.did.verifier.v1.protocol.VerifierService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SDK VerifierService Bean Configuration
 *
 * Registers the SDK's VerifierService as a Spring bean.
 * Creates the VerifierService from the seven injected interface implementations (adapters).
 */
@Configuration
public class VerifierSdkConfig {

    /**
     * SDK VerifierService Bean
     *
     * @param configProvider         Provider for policy/profile lookup
     * @param verifierInfoProvider   Provider for verifier information
     * @param sessionProvider        Provider for E2E sessions
     * @param storageService         Service for DID Document/VC Meta lookup
     * @param transactionManager     Transaction manager
     * @param nonceGenerator         Nonce generator
     * @param cryptoHelper           Cryptography helper
     * @return VerifierService (SDK Facade)
     */
    @Bean
    public VerifierService verifierService(
            VerificationConfigProvider configProvider,
            VerifierInfoProvider verifierInfoProvider,
            EcdhSessionProvider sessionProvider,
            StorageProvider storageService,
            TransactionProvider transactionManager,
            NonceGenerator nonceGenerator,
            CryptoHelper cryptoHelper
    ) {
        return new VerifierSdkBuilder()
                .configProvider(configProvider)
                .verifierInfoProvider(verifierInfoProvider)
                .sessionProvider(sessionProvider)
                .storageProvider(storageService)
                .transactionProvider(transactionManager)
                .nonceGenerator(nonceGenerator)
                .cryptoHelper(cryptoHelper)
                .build();
    }
}
