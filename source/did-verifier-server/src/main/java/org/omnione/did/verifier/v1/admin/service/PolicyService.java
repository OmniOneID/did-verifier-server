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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.constant.PolicyType;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.db.repository.ZkpPolicyProfileRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;
import org.omnione.did.verifier.v1.admin.dto.VpSubmitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

}
