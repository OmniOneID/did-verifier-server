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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.datamodel.enums.ProfileType;
import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.omnione.did.base.db.repository.ZkpPolicyProfileRepository;
import org.omnione.did.verifier.v1.admin.dto.ZkpPolicyProfileDto;
import org.omnione.did.verifier.v1.common.dto.EmptyResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ZkpProfileService {
    private final ZkpPolicyProfileQueryService zkpPolicyProfileQueryService;
    private final ZkpProofRequestQueryService zkpProofRequestQueryService;
    private final ZkpPolicyProfileRepository zkpPolicyProfileRepository;

    public Page<ZkpPolicyProfileDto> searchZkpProfileList(String searchKey, String searchValue, Pageable pageable) {
        return zkpPolicyProfileQueryService.searchZkpProfileList(searchKey, searchValue, pageable);
    }

    public EmptyResDto createZkpProfile(ZkpPolicyProfileDto zkpPolicyProfileDto) {
        zkpPolicyProfileRepository.save(
            ZkpPolicyProfile.builder()
                .profileId(UUID.randomUUID().toString())
                .title(zkpPolicyProfileDto.getTitle())
                .type(ProfileType.PROOF_REQUEST_PROFILE)
                .description(zkpPolicyProfileDto.getDescription())
                .encoding(zkpPolicyProfileDto.getEncoding())
                    .language(zkpPolicyProfileDto.getLanguage())
                    .zkpProofRequestId(zkpPolicyProfileDto.getZkpProofRequestId())
                .build()
        );
        return new EmptyResDto();
    }

    public ZkpPolicyProfileDto getZkpProfileInfo(Long id) {
        ZkpPolicyProfile zkpPolicyProfile = zkpPolicyProfileQueryService.findById(id);
        ZkpProofRequest zkpProofRequest = zkpProofRequestQueryService.findById(zkpPolicyProfile.getZkpProofRequestId());

        ZkpPolicyProfileDto zkpPolicyProfileDto = ZkpPolicyProfileDto.fromDomain(zkpPolicyProfile);
        zkpPolicyProfileDto.setZkpProofRequestName(zkpProofRequest.getName());
        zkpPolicyProfileDto.setZkpProofRequestId(zkpProofRequest.getId());

        return zkpPolicyProfileDto;
    }

    public EmptyResDto updateZkpProfile(ZkpPolicyProfileDto zkpPolicyProfileDto) {
        ZkpPolicyProfile zkpPolicyProfile = zkpPolicyProfileQueryService.findById(zkpPolicyProfileDto.getId());

        zkpPolicyProfile.setTitle(zkpPolicyProfileDto.getTitle());
        zkpPolicyProfile.setDescription(zkpPolicyProfileDto.getDescription());
        zkpPolicyProfile.setEncoding(zkpPolicyProfileDto.getEncoding());
        zkpPolicyProfile.setLanguage(zkpPolicyProfileDto.getLanguage());
        zkpPolicyProfile.setZkpProofRequestId(zkpPolicyProfileDto.getZkpProofRequestId());

        zkpPolicyProfileRepository.save(zkpPolicyProfile);

        return new EmptyResDto();
    }

    public EmptyResDto deleteZkpProfile(Long id) {
        zkpPolicyProfileRepository.deleteById(id);
        return new EmptyResDto();
    }
}
