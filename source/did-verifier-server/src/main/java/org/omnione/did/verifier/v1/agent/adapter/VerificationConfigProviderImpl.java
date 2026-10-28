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

package org.omnione.did.verifier.v1.agent.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.base.db.repository.VpProcessRepository;
import org.omnione.did.verifier.v1.common.PolicyCacheService;
import org.omnione.did.data.model.profile.Filter;
import org.omnione.did.data.model.profile.verify.VerifyProcess;
import org.omnione.did.data.model.vc.CredentialSchema;
import org.omnione.did.verifier.v1.provider.VerificationConfigProvider;
import org.omnione.did.verifier.v1.model.policy.VerificationPolicy;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;

/**
 * SDK VerificationConfigProvider implementation (adapter pattern).
 *
 * Retrieves policy, filter and process information from the DB and converts it into the SDK DTO (VerificationPolicy).
 * The filter and process fields of the resulting DTO use the core library types directly.
 *
 * Design principles:
 * - The adapter only performs conversion (no business logic)
 * - Core library types are returned directly (no intermediate SDK DTOs)
 * - Fields are mapped directly without JSON serialization
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationConfigProviderImpl implements VerificationConfigProvider {

    private final PolicyCacheService policyCacheService;
    private final PolicyProfileRepository policyProfileRepository;
    private final VpFilterRepository vpFilterRepository;
    private final VpProcessRepository vpProcessRepository;
    private final PayloadRepository payloadRepository;
    private final ObjectMapper objectMapper;

    /**
     * Retrieves the verification policy for a policy ID.
     *
     * @param policyId Policy ID
     * @return VerificationPolicy (filter, process and verifier are core types)
     * @throws VerifierSdkException if the policy does not exist or the lookup fails
     */
    @Override
    public VerificationPolicy getPolicy(String policyId) {
        try {
            // 1. Look up policy (cached)
            Policy policy;
            try {
                policy = policyCacheService.findByPolicyId(policyId);
            } catch (Exception e) {
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_POLICY_NOT_FOUND,
                        "Policy not found: " + policyId);
            }

            // 2. Check PolicyType - ZKP policies cannot be retrieved by this method
            if (policy.getPolicyType() == org.omnione.did.base.db.constant.PolicyType.ZKP) {
                log.warn("getPolicy() called with ZKP Policy ID: {}. This method only supports VP Policy.", policyId);
                throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_POLICY_NOT_FOUND,
                    "ZKP Policy cannot be retrieved through this method. " +
                    "Policy ID: " + policyId + ". " +
                    "Use /request-proof-request-profile API for ZKP verification."
                );
            }

            // 3. Look up PolicyProfile (VP policies only)
            PolicyProfile policyProfile = policyProfileRepository.findByPolicyProfileId(policy.getPolicyProfileId())
                    .orElseThrow(() -> new VerifierSdkException(
                            VerifierSdkErrorCode.SDK_INVALID_POLICY_CONFIGURATION,
                            "Policy profile not found: " + policy.getPolicyProfileId()));

            // 4. Look up VpFilter
            VpFilter vpFilter = vpFilterRepository.findById(policyProfile.getFilterId())
                    .orElseThrow(() -> new VerifierSdkException(
                            VerifierSdkErrorCode.SDK_INVALID_POLICY_CONFIGURATION,
                            "Filter not found for ID: " + policyProfile.getFilterId()));

            // 5. Look up VpProcess
            VpProcess vpProcess = vpProcessRepository.findById(policyProfile.getProcessId())
                    .orElseThrow(() -> new VerifierSdkException(
                            VerifierSdkErrorCode.SDK_INVALID_POLICY_CONFIGURATION,
                            "Process not found for ID: " + policyProfile.getProcessId()));

            // 6. Look up Payload
            Payload payload = payloadRepository.findByPayloadId(policy.getPayloadId())
                    .orElseThrow(() -> new VerifierSdkException(
                            VerifierSdkErrorCode.SDK_INVALID_POLICY_CONFIGURATION,
                            "Payload not found for ID: " + policy.getPayloadId()));

            // 7. Assemble VerificationPolicy DTO (using core types directly)
            return buildVerificationPolicy(policy, policyProfile, vpFilter, vpProcess, payload);

        } catch (VerifierSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve policy: {}", policyId, e);
            throw new VerifierSdkException(VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                    "Failed to retrieve policy configuration: " + e.getMessage());
        }
    }

    @Override
    public boolean existsPolicy(String policyId) {
        try {
            return policyCacheService.findOptionalByPolicyId(policyId).isPresent();
        } catch (Exception e) {
            log.debug("Error checking policy existence: {}", policyId, e);
            return false;
        }
    }

    /**
     * Assembles Policy, PolicyProfile, VpFilter, VpProcess and Payload into a VerificationPolicy DTO.
     * filter and process are built directly as core library types.
     */
    private VerificationPolicy buildVerificationPolicy(
            Policy policy,
            PolicyProfile policyProfile,
            VpFilter vpFilter,
            VpProcess vpProcess,
            Payload payload) {

        return VerificationPolicy.builder()
                .policyId(policy.getPolicyId())
                .policyName(policy.getPolicyTitle())
                .description(policyProfile.getDescription())
                .language(policyProfile.getLanguage())
                .mode(payload.getMode() != null ? payload.getMode().toString() : "Direct")
                .validityDuration(payload.getValidSecond())
                .endpoints(parseEndpoints(payload.getEndpoints()))
                // Built directly as core types (no intermediate SDK DTO)
                .filter(buildFilter(vpFilter))
                .process(buildVerifyProcess(vpProcess))
                .build();
    }

    /**
     * Parses the endpoints JSON string into a List.
     */
    private List<String> parseEndpoints(String endpointsJson) {
        try {
            if (endpointsJson == null || endpointsJson.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(endpointsJson, List.class);
        } catch (Exception e) {
            log.warn("Failed to parse endpoints JSON: {}", endpointsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * VpFilter entity -> core Filter (direct field mapping, no JSON serialization).
     */
    private Filter buildFilter(VpFilter vpFilter) {
        CredentialSchema schema = new CredentialSchema();
        schema.setId(vpFilter.getId());
        schema.setType(vpFilter.getType());
        schema.setValue(vpFilter.getValue());
        schema.setPresentAll(vpFilter.isPresent_all());
        schema.setDisplayClaims(vpFilter.getDisplayClaims() != null
                ? vpFilter.getDisplayClaims()
                : Collections.emptyList());
        schema.setRequiredClaims(vpFilter.getRequiredClaims() != null
                ? vpFilter.getRequiredClaims()
                : Collections.emptyList());
        schema.setAllowedIssuers(vpFilter.getAllowedIssuers() != null
                ? vpFilter.getAllowedIssuers()
                : Collections.emptyList());

        Filter filter = new Filter();
        filter.setCredentialSchemas(List.of(schema));
        return filter;
    }

    /**
     * VpProcess entity -> core VerifyProcess (basic information only, excluding reqE2e/verifierNonce).
     * reqE2e and verifierNonce are added dynamically at runtime by the SDK's DefaultVpProfileService.
     */
    private VerifyProcess buildVerifyProcess(VpProcess vpProcess) {
        VerifyProcess process = new VerifyProcess();
        process.setEndpoints(vpProcess.getEndpoints() != null
                ? vpProcess.getEndpoints()
                : Collections.emptyList());
        process.setAuthType(vpProcess.getAuthType());
        return process;
    }
}
