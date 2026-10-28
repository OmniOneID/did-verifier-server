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

package org.omnione.did.base.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties class for the Verifier.
 * This class maps configuration properties with the prefix "verifier" to its fields.
 *
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "verifier")
public class VerifierProperty {
    private String did;
    private String certVcRef;
    private String ref;
    private String name;
    private String domain;
    private String tasUrl;
    private ArrayList<String> verifierEndPoints = new ArrayList<>();

    @Getter
    @Setter
    public static class StatusListProperties {
        private boolean failOnFetchError = true;
        private long maxCacheTtlSeconds = 86400;
        private long minCacheTtlSeconds = 60;
        // Maximum decompressed size in bytes. A status list with one million entries is about 125KB at bits=8,
        // so 1MB comfortably accepts any legitimate status list while blocking decompression bombs.
        private long maxDecompressedBytes = 1_048_576;
        // Private-network/loopback hosts explicitly trusted by the deployer (exact host string match).
        // Register a host only when a private status list host is expected, e.g. closed-network deployments or local development.
        private List<String> allowedPrivateHosts = new ArrayList<>();
    }

    private StatusListProperties statusList = new StatusListProperties();
}
