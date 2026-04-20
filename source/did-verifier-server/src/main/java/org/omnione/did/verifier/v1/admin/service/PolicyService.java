/*
 * Copyright 2024 - 2025 OmniOne.
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.constant.PolicyType;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.base.db.repository.VpProcessRepository;
import org.omnione.did.base.db.repository.ZkpPolicyProfileRepository;
import org.omnione.did.base.db.repository.ZkpProofRequestRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;
import org.omnione.did.verifier.v1.admin.dto.VpSubmitDTO;
import org.omnione.did.verifier.v1.agent.helper.PublishCertificateHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The VpPolicyServiceImpl class provides methods for querying the database for VP policies.
 * It is designed to facilitate the retrieval of VP policies from the database, ensuring that the data is accurate and up-to-date.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final PayloadRepository payloadRepository;
    private final PolicyProfileRepository policyProfileRepository;
    private final PolicyQueryService policyQueryService;
    private final ZkpPolicyProfileRepository zkpPolicyProfileRepository;
    private final VpFilterRepository vpFilterRepository;
    private final VpProcessRepository vpProcessRepository;
    private final VerifierInfoQueryService verifierInfoQueryService;
    private final ZkpProofRequestRepository zkpProofRequestRepository;

    public PolicyDTO getPolicyInfo(Long id, PolicyType policyType) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));
        return convertToPolicyDTO(policy, policyType);
    }


    public void savePolicy(PolicyDTO policyDTO, PolicyType policyType) {
        Policy policy = Policy.builder()
                .policyId(UUID.randomUUID().toString())
                .payloadId(policyDTO.getPayloadId())
                .policyProfileId(policyDTO.getPolicyProfileId())
                .policyTitle(policyDTO.getPolicyTitle())
                .policyType(policyType)
                .build();

        policyRepository.save(policy);
    }


    public PolicyDTO updatePolicy(PolicyDTO policyDTO) {
        Policy findPolicy = policyRepository.findById(policyDTO.getId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));
            findPolicy.setPayloadId(policyDTO.getPayloadId());
            findPolicy.setPolicyProfileId(policyDTO.getPolicyProfileId());
            findPolicy.setPolicyTitle(policyDTO.getPolicyTitle());
            Policy savedPolicy = policyRepository.save(findPolicy);
            return PolicyDTO.toDTO(savedPolicy);
    }

    public void deletePolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));
        policyRepository.delete(policy);
    }

    private PolicyDTO convertToPolicyDTO(Policy policy, PolicyType policyType) {
        String payloadService = payloadRepository.findByPayloadId(policy.getPayloadId())
                .map(Payload::getService)
                .orElse("Unknown Payload Service");

        String profileTitle = (policyType == PolicyType.ZKP)
                ? zkpPolicyProfileRepository.findByProfileId(policy.getPolicyProfileId())
                .map(ZkpPolicyProfile::getTitle)
                .orElse("Unknown Profile Title")
                : policyProfileRepository.findByPolicyProfileId(policy.getPolicyProfileId())
                        .map(PolicyProfile::getTitle)
                        .orElse("Unknown Profile Title");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return PolicyDTO.builder()
                .id(policy.getId())
                .policyId(policy.getPolicyId())
                .payloadId(policy.getPayloadId())
                .policyProfileId(policy.getPolicyProfileId())
                .policyTitle(policy.getPolicyTitle())
                .payloadService(payloadService)
                .profileTitle(profileTitle)
                .createdAt(formatInstant(policy.getCreatedAt(), formatter))
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        return VpSubmitDTO.formatInstant(instant, formatter);
    }


    public Page<PolicyDTO> searchPolicyList(String searchKey, String searchValue, Pageable pageable, PolicyType policyType) {
        return policyQueryService.searchPolicyProfileList(searchKey, searchValue, pageable, policyType);
    }

    public List<PolicyDTO> getAllPolicies() {
        List<Policy> policies = policyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return policies.stream()
                .map(policy -> convertToPolicyDTO(policy, policy.getPolicyType()))
                .toList();
    }

    public Object getPolicyVerifyProfile(Long id, PolicyType policyType) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));

        Payload payload = payloadRepository.findByPayloadId(policy.getPayloadId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));

        VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();

        if (policyType == PolicyType.ZKP) {
            return buildZkpProfile(policy, payload, verifierInfo);
        }

        PolicyProfile policyProfile = policyProfileRepository.findByPolicyProfileId(policy.getPolicyProfileId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));

        VpFilter vpFilter = vpFilterRepository.findByFilterId(policyProfile.getFilterId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND));

        VpProcess vpProcess = vpProcessRepository.findById(policyProfile.getProcessId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROCESS_NOT_FOUND));

        Map<String, Object> completePolicy = new LinkedHashMap<>();
        completePolicy.put("policyId", policy.getPolicyId());

        Map<String, Object> payloadSection = new LinkedHashMap<>();
        payloadSection.put("device", payload.getDevice());
        payloadSection.put("service", payload.getService());

        if (payload.getEndpoints().startsWith("[")) {
            String endpointsStr = payload.getEndpoints().replace("[", "").replace("]", "").replace("\"", "");
            payloadSection.put("endpoints", Arrays.asList(endpointsStr.split(",")));
        } else {
            payloadSection.put("endpoints", Arrays.asList(payload.getEndpoints()));
        }

        payloadSection.put("locked", payload.isLocked());
        payloadSection.put("mode", payload.getMode() != null ? payload.getMode().name() : "Direct");
        completePolicy.put("payload", payloadSection);

        Map<String, Object> profileSection = new LinkedHashMap<>();
        profileSection.put("id", "");
        profileSection.put("type", "VerifyProfile");
        profileSection.put("title", policyProfile.getTitle());
        profileSection.put("description", policyProfile.getDescription());
        profileSection.put("encoding", policyProfile.getEncoding());
        profileSection.put("language", policyProfile.getLanguage());

        Map<String, Object> innerProfile = new LinkedHashMap<>();

        Map<String, Object> verifier = new LinkedHashMap<>();
        verifier.put("did", verifierInfo.getDid());
        verifier.put("certVcRef", PublishCertificateHelper.getCertificateVcURL(verifierInfo));
        verifier.put("name", verifierInfo.getName());
        verifier.put("description", verifierInfo.getName());
        verifier.put("ref", verifierInfo.getServerUrl());
        innerProfile.put("verifier", verifier);

        Map<String, Object> filter = new LinkedHashMap<>();
        if (vpFilter.getValue() != null && vpFilter.getValue().trim().startsWith("{")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> parsedValue = objectMapper.readValue(
                        vpFilter.getValue(), new TypeReference<Map<String, Object>>() {});
                filter.put("credentialSchemas", parsedValue);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse filter value as JSON: {}", e.getMessage());
                addSimpleFilterStructure(filter, vpFilter);
            }
        } else {
            addSimpleFilterStructure(filter, vpFilter);
        }
        innerProfile.put("filter", filter);

        Map<String, Object> process = new LinkedHashMap<>();
        process.put("endpoints", vpProcess.getEndpoints() != null ? vpProcess.getEndpoints() : Arrays.asList());

        Map<String, Object> reqE2e = new LinkedHashMap<>();
        reqE2e.put("nonce", "");
        reqE2e.put("curve", vpProcess.getCurve() != null ? vpProcess.getCurve().name() : "Secp256r1");
        reqE2e.put("publicKey", "");
        reqE2e.put("cipher", vpProcess.getCipher() != null ? vpProcess.getCipher().name() : "AES-256-CBC");
        reqE2e.put("padding", vpProcess.getPadding() != null ? vpProcess.getPadding().name() : "PKCS5");
        process.put("reqE2e", reqE2e);

        process.put("verifierNonce", "");
        process.put("authType", vpProcess.getAuthType());
        innerProfile.put("process", process);

        profileSection.put("profile", innerProfile);
        completePolicy.put("profile", profileSection);

        return completePolicy;
    }

    private Map<String, Object> buildZkpProfile(Policy policy, Payload payload, VerifierInfo verifierInfo) {
        ZkpPolicyProfile zkpPolicyProfile = zkpPolicyProfileRepository.findByProfileId(policy.getPolicyProfileId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.ZKP_POLICY_PROFILE_NOT_FOUND));

        ZkpProofRequest zkpProofRequest = zkpProofRequestRepository.findById(zkpPolicyProfile.getZkpProofRequestId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.ZKP_PROOF_REQUEST_NOT_FOUND));

        Map<String, Object> completePolicy = new LinkedHashMap<>();
        completePolicy.put("policyId", policy.getPolicyId());

        Map<String, Object> payloadSection = new LinkedHashMap<>();
        payloadSection.put("device", payload.getDevice());
        payloadSection.put("service", payload.getService());
        if (payload.getEndpoints().startsWith("[")) {
            String endpointsStr = payload.getEndpoints().replace("[", "").replace("]", "").replace("\"", "");
            payloadSection.put("endpoints", Arrays.asList(endpointsStr.split(",")));
        } else {
            payloadSection.put("endpoints", Arrays.asList(payload.getEndpoints()));
        }
        payloadSection.put("locked", payload.isLocked());
        payloadSection.put("mode", payload.getMode() != null ? payload.getMode().name() : "Direct");
        completePolicy.put("payload", payloadSection);

        Map<String, Object> profileSection = new LinkedHashMap<>();
        profileSection.put("id", "");
        profileSection.put("type", zkpPolicyProfile.getType() != null ? zkpPolicyProfile.getType().name() : "ProofRequestProfile");
        profileSection.put("title", zkpPolicyProfile.getTitle());
        profileSection.put("description", zkpPolicyProfile.getDescription());
        profileSection.put("encoding", zkpPolicyProfile.getEncoding());
        profileSection.put("language", zkpPolicyProfile.getLanguage());

        Map<String, Object> innerProfile = new LinkedHashMap<>();

        Map<String, Object> verifier = new LinkedHashMap<>();
        verifier.put("did", verifierInfo.getDid());
        verifier.put("certVcRef", PublishCertificateHelper.getCertificateVcURL(verifierInfo));
        verifier.put("name", verifierInfo.getName());
        verifier.put("description", verifierInfo.getName());
        verifier.put("ref", verifierInfo.getServerUrl());
        innerProfile.put("verifier", verifier);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> proofRequest = new LinkedHashMap<>();
        proofRequest.put("name", zkpProofRequest.getName());
        proofRequest.put("version", zkpProofRequest.getVersion());
        proofRequest.put("nonce", "");

        if (zkpProofRequest.getRequestedAttributes() != null && !zkpProofRequest.getRequestedAttributes().isEmpty()) {
            try {
                proofRequest.put("requestedAttributes", objectMapper.readValue(
                        zkpProofRequest.getRequestedAttributes(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse requestedAttributes as JSON: {}", e.getMessage());
                proofRequest.put("requestedAttributes", zkpProofRequest.getRequestedAttributes());
            }
        }
        if (zkpProofRequest.getRequestedPredicates() != null && !zkpProofRequest.getRequestedPredicates().isEmpty()) {
            try {
                proofRequest.put("requestedPredicates", objectMapper.readValue(
                        zkpProofRequest.getRequestedPredicates(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse requestedPredicates as JSON: {}", e.getMessage());
                proofRequest.put("requestedPredicates", zkpProofRequest.getRequestedPredicates());
            }
        }
        innerProfile.put("proofRequest", proofRequest);

        Map<String, Object> reqE2e = new LinkedHashMap<>();
        reqE2e.put("nonce", "");
        reqE2e.put("curve", zkpProofRequest.getCurve() != null ? zkpProofRequest.getCurve().name() : "Secp256r1");
        reqE2e.put("publicKey", "");
        reqE2e.put("cipher", zkpProofRequest.getCipher() != null ? zkpProofRequest.getCipher().name() : "AES-256-CBC");
        reqE2e.put("padding", zkpProofRequest.getPadding() != null ? zkpProofRequest.getPadding().name() : "PKCS5");
        innerProfile.put("reqE2e", reqE2e);

        profileSection.put("profile", innerProfile);
        completePolicy.put("profile", profileSection);

        return completePolicy;
    }

    private void addSimpleFilterStructure(Map<String, Object> filter, VpFilter vpFilter) {
        filter.put("title", vpFilter.getTitle());
        filter.put("type", vpFilter.getType());
        filter.put("requiredClaims", vpFilter.getRequiredClaims());
        filter.put("allowedIssuers", vpFilter.getAllowedIssuers());
        filter.put("displayClaims", vpFilter.getDisplayClaims());
        filter.put("value", vpFilter.getValue());
        filter.put("presentAll", vpFilter.isPresent_all());
    }

}
