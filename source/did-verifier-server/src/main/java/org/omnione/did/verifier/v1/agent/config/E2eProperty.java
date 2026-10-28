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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * E2E encryption settings.
 * Maps the e2e section of application.yml.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "e2e")
public class E2eProperty {
    /**
     * ECC Curve Type
     * Default: Secp256r1
     */
    private String curve = "Secp256r1";

    /**
     * Symmetric encryption algorithm
     * Default: AES-256-CBC
     */
    private String cipher = "AES-256-CBC";

    /**
     * Padding scheme
     * Default: PKCS5
     */
    private String padding = "PKCS5";
}
