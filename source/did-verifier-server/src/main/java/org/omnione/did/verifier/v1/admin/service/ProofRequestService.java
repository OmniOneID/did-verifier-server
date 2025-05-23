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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.omnione.did.base.db.repository.ZkpProofRequestRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.api.ListFeign;
import org.omnione.did.verifier.v1.admin.api.dto.ListCredentialSchemaDto;
import org.omnione.did.verifier.v1.admin.dto.ProofRequestDto;
import org.omnione.did.verifier.v1.admin.dto.VerifyUniqueResDto;
import org.omnione.did.verifier.v1.admin.dto.ZkpProofRequestDto;
import org.omnione.did.verifier.v1.common.dto.EmptyResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProofRequestService {
    private final ZkpProofRequestQueryService zkpProofRequestQueryService;
    private final ListFeign listFeign;
    private final ZkpProofRequestRepository zkpProofRequestRepository;

    public Page<ZkpProofRequestDto> searchProofRequestList(String searchKey, String searchValue, Pageable pageable) {
        return zkpProofRequestQueryService.searchProofRequestList(searchKey, searchValue, pageable);
    }

    public List<ListCredentialSchemaDto> getCredentialSchemaList() {
        try {
            List<ListCredentialSchemaDto> listCredentialSchemaDtos = listFeign.requestCredentialSchemaList();
            return listFeign.requestCredentialSchemaList();
        } catch (OpenDidException e) {
            throw new OpenDidException(ErrorCode.CREDENTIAL_SCHEMA_NOT_FOUND);
        } catch (Exception e) {
            throw new OpenDidException(ErrorCode.CREDENTIAL_SCHEMA_NOT_FOUND);
        }
    }

    public EmptyResDto createProofRequest(ProofRequestDto request) {
        ObjectMapper objectMapper = new ObjectMapper();

        String requestedAttributesJson;
        String requestedPredicatesJson;

        try {
            requestedAttributesJson = objectMapper.writeValueAsString(request.getRequestedAttributes());
            requestedPredicatesJson = objectMapper.writeValueAsString(request.getRequestedPredicates());
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }

        zkpProofRequestRepository.save(ZkpProofRequest.builder()
                .name(request.getName())
                .version(request.getVersion())
                .curve(request.getCurve())
                .cipher(request.getCipher())
                .padding(request.getPadding())
                .requestedPredicates(requestedPredicatesJson)
                .requestedAttributes(requestedAttributesJson)
                .build()
        );

        return new EmptyResDto();
    }

    public VerifyUniqueResDto verifyNameUnique(String name) {
        long count = zkpProofRequestQueryService.countByName(name);
        return VerifyUniqueResDto.builder()
                .isUnique(count == 0)
                .build();
    }
}
