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
package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.constant.PolicyType;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.db.repository.ZkpPolicyProfileRepository;
import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;
import org.omnione.did.verifier.v1.admin.dto.PolicyProfileDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PolicyQueryService {
    private final PolicyRepository policyRepository;
    private final PayloadRepository payloadRepository;
    private final PolicyProfileRepository policyProfileRepository;
    private final ZkpPolicyProfileRepository zkpPolicyProfileRepository;


    public Page<PolicyDTO> searchPolicyProfileList(String searchKey, String searchValue, Pageable pageable, PolicyType policyType) {
        Page<Policy> policies = policyRepository.searchPolicyList(searchKey, searchValue, policyType, pageable);

        List<PolicyDTO> policyDTOs = policies.getContent().stream().map(policy -> {
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

            return PolicyDTO.toDTO(policy, payloadService, profileTitle);
        }).collect(Collectors.toList());

        return new PageImpl<>(policyDTOs, pageable, policies.getTotalElements());
    }
}
