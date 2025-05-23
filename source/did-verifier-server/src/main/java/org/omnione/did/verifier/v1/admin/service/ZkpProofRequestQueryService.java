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
import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.omnione.did.base.db.repository.ZkpPolicyProfileRepository;
import org.omnione.did.base.db.repository.ZkpProofRequestRepository;
import org.omnione.did.base.db.repository.projection.ZkpProofRequestIdProjection;
import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;
import org.omnione.did.verifier.v1.admin.dto.VerifyUniqueResDto;
import org.omnione.did.verifier.v1.admin.dto.ZkpProofRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ZkpProofRequestQueryService {
    private final ZkpProofRequestRepository zkpProofRequestRepository;
    private final ZkpPolicyProfileRepository zkpPolicyProfileRepository;

    public Page<ZkpProofRequestDto> searchProofRequestList(String searchKey, String searchValue, Pageable pageable) {

        Page<ZkpProofRequest> zkpProofRequestPage = zkpProofRequestRepository.searchZkpProofRequestList(searchKey, searchValue, pageable);
        List<ZkpProofRequest> zkpProofRequestList = zkpProofRequestPage.getContent();

        // 1. Extract list of IDs
        List<Long> zkpProofRequestIds = zkpProofRequestList.stream()
                .map(ZkpProofRequest::getId)
                .toList();

        // 2. Fetch counts in batch
        List<ZkpProofRequestIdProjection> countResults = zkpPolicyProfileRepository.countByZkpProofRequestIdIn(zkpProofRequestIds);

        // 3. Convert results to a Map
        Map<Long, Long> countMap = countResults.stream()
                .collect(Collectors.toMap(ZkpProofRequestIdProjection::getZkpProofRequestId, ZkpProofRequestIdProjection::getCount));

        // 4. Convert to DTOs
        List<ZkpProofRequestDto> zkpNamespaceDtos = zkpProofRequestList.stream()
                .map(ns -> {
                    Long count = countMap.getOrDefault(ns.getId(), 0L);
                    return ZkpProofRequestDto.fromDomain(ns, count);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(zkpNamespaceDtos, pageable, zkpProofRequestPage.getTotalElements());
    }

    public long countByName(String name) {
        return zkpProofRequestRepository.countByName(name);
    }
}
