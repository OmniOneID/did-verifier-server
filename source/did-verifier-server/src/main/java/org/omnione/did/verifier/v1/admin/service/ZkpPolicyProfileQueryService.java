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
import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.omnione.did.base.db.repository.PolicyProfileIdProjection;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.db.repository.ZkpPolicyProfileRepository;
import org.omnione.did.base.db.repository.ZkpProofRequestRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.ZkpPolicyProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ZkpPolicyProfileQueryService {
    private final ZkpPolicyProfileRepository zkpPolicyProfileRepository;
    private final ZkpProofRequestRepository zkpProofRequestRepository;
    private final PolicyRepository policyRepository;

    public Page<ZkpPolicyProfileDto> searchZkpProfileList(String searchKey, String searchValue, Pageable pageable) {

        Page<ZkpPolicyProfile> zkpPolicyProfilePage = zkpPolicyProfileRepository.searchZkpPolicyProfileList(searchKey, searchValue, pageable);
        List<ZkpPolicyProfile> zkpPolicyProfileList = zkpPolicyProfilePage.getContent();

        // 1. Extract list of ProfileID
        List<String> profileIds = zkpPolicyProfileList.stream()
                .map(ZkpPolicyProfile::getProfileId)
                .toList();

        // 2. Fetch counts in batch
        List<PolicyProfileIdProjection> countResults = policyRepository.countByPolicyProfileIdIn(profileIds);

        // 3. Convert results to a Map
        Map<String, Long> countMap = countResults.stream()
                .collect(Collectors.toMap(PolicyProfileIdProjection::getPolicyProfileId, PolicyProfileIdProjection::getCount));

        // 4. Convert to DTOs
        List<ZkpPolicyProfileDto> zkpPolicyProfileDtos = zkpPolicyProfileList.stream()
                .map(zkpPolicyProfile -> {
                    Long count = countMap.getOrDefault(zkpPolicyProfile.getProfileId(), 0L);
                    return ZkpPolicyProfileDto.fromDomain(zkpPolicyProfile, count);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(zkpPolicyProfileDtos, pageable, zkpPolicyProfilePage.getTotalElements());
    }

    public ZkpPolicyProfile findById(Long id) {
        return zkpPolicyProfileRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.ZKP_POLICY_PROFILE_NOT_FOUND));
    }
}
