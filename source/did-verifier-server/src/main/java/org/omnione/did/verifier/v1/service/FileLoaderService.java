/*
 * Copyright 2024 OmniOne.
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

package org.omnione.did.verifier.v1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.omnione.did.base.datamodel.data.VerifyOfferPayload;
import org.omnione.did.base.datamodel.data.VpPolicy;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.omnione.did.data.model.profile.verify.VerifyProcess;
import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.data.model.provider.ProviderDetail;
import org.omnione.did.verifier.v1.dto.RequestOfferReqDto;
import org.omnione.did.verifier.v1.dto.VerifyOfferResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for loading VP policies and verification offer payloads from JSON files.
 *
 * This service loads and caches policies and payloads from a specified directory,
 * and provides methods to retrieve them by policy ID or by offer data.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileLoaderService {

    private final ObjectMapper objectMapper;
    private final VerifierProperty verifierProperty;
    private final Map<String, VpPolicy> policyCache = new HashMap<>();

    @Value("${verifier.sample-path}")
    private String samplePath;

    /**
     * Loads VP policies and their payloads from JSON files located in the configured sample path.
     * This method is called after the service is constructed to populate the cache.
     * @throws OpenDidException if an error occurs while loading the policies.
     */
    @PostConstruct
    public void loadPolicies() {
        try {
            Path startPath = Paths.get(samplePath);
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (isJsonFile(file)) {
                        processJsonFile(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            log.info("Loaded {} policies into cache", policyCache.size());
        } catch (IOException e) {
            log.error("Error loading policies: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.VP_POLICY_READ_ERROR);
        }
    }

    private boolean isJsonFile(Path file) {
        return file.toString().toLowerCase().endsWith(".json");
    }

    private void processJsonFile(Path file) {
        try {
            VpPolicy vpPolicy = objectMapper.readValue(file.toFile(), VpPolicy.class);
            policyCache.put(vpPolicy.getPolicyId(), vpPolicy);
        } catch (IOException e) {
            log.error("Error processing JSON file {}: {}", file, e.getMessage());
        }
    }

    /**
     * Retrieves a VP policy by its policy ID from the cache.
     *
     * @param policyId The ID of the policy to retrieve.
     * @return The VP policy corresponding to the given policy ID.
     * @throws OpenDidException if the policy is not found in the cache.
     */
    public VpPolicy getPolicyById(String policyId) {
        VpPolicy policy = policyCache.get(policyId);
        if (policy == null) {
            log.error("VpPolicy not found for policyId: {}", policyId);
            throw new OpenDidException(ErrorCode.VP_POLICY_NOT_FOUND);
        }
        ProviderDetail providerDetail = policy.getProfile().getProfile().getVerifier();
        providerDetail.setCertVcRef(verifierProperty.getCertVcRef());
        providerDetail.setDid(verifierProperty.getDid());
        providerDetail.setRef(verifierProperty.getRef());
        VerifyProcess process = policy.getProfile().getProfile().getProcess();
        process.setEndpoints(verifierProperty.getVerifierEndPoints());

        return policy;
    }

    /**
     * Retrieves a VP policy from the cache by matching offer data.
     *
     * @param requestOfferReqDto Request offer data containing device, service, and mode.
     * @return The VP policy matching the given offer data.
     * @throws OpenDidException if no matching policy is found.
     */
    public VerifyOfferResult getPolicyByOfferData(RequestOfferReqDto requestOfferReqDto) {
        for (VpPolicy vpPolicy : policyCache.values()) {
            if (matchesPolicy(vpPolicy, requestOfferReqDto)) {
                return buildVerifyOfferResult(vpPolicy);
            }
        }

        log.error("VpPolicy not found for mode: {}, service: {}, device: {}",
                requestOfferReqDto.getMode(), requestOfferReqDto.getService(), requestOfferReqDto.getDevice());
        throw new OpenDidException(ErrorCode.VP_POLICY_NOT_FOUND);
    }

    private boolean matchesPolicy(VpPolicy vpPolicy, RequestOfferReqDto requestOfferReqDto) {
        VerifyOfferPayload payload = vpPolicy.getPayload();
        return StringUtils.equalsIgnoreCase(payload.getDevice(), requestOfferReqDto.getDevice())
                && StringUtils.equalsIgnoreCase(payload.getService(), requestOfferReqDto.getService())
                && StringUtils.equalsIgnoreCase(String.valueOf(payload.getMode()), String.valueOf(requestOfferReqDto.getMode()));
    }

    private VerifyOfferResult buildVerifyOfferResult(VpPolicy vpPolicy) {
        VerifyOfferPayload offerPayload = vpPolicy.getPayload();
        offerPayload.setEndpoints(verifierProperty.getVerifierEndPoints());
        return VerifyOfferResult.builder()
                .vpPolicyId(vpPolicy.getPolicyId())
                .payload(vpPolicy.getPayload())
                .build();
    }
}