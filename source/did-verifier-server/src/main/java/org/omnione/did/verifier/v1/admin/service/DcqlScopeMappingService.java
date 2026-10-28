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

package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.DcqlScopeMapping;
import org.omnione.did.base.db.repository.DcqlScopeMappingRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.oid4vc.oid4vp.service.ScopeToDCQLMapperService;
import org.omnione.did.verifier.v1.admin.api.ListFeign;
import org.omnione.did.verifier.v1.admin.api.dto.ListCredentialSchemaDto;
import org.omnione.did.verifier.v1.admin.dto.DcqlScopeMappingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DcqlScopeMappingService {

    private final DcqlScopeMappingRepository dcqlScopeMappingRepository;
    private final ScopeToDCQLMapperService scopeToDCQLMapperService;
    private final ListFeign listFeign;

    public Page<DcqlScopeMappingDTO> searchScopeMappings(Pageable pageable) {
        return dcqlScopeMappingRepository.findAll(pageable)
                .map(DcqlScopeMappingDTO::toDTO);
    }

    public DcqlScopeMappingDTO getScopeMapping(Long id) {
        DcqlScopeMapping entity = dcqlScopeMappingRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND));
        return DcqlScopeMappingDTO.toDTO(entity);
    }

    public void saveScopeMapping(DcqlScopeMappingDTO dto) {
        if (dcqlScopeMappingRepository.findByScope(dto.getScope()).isPresent()) {
            throw new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND);
        }

        DcqlScopeMapping entity = DcqlScopeMapping.builder()
                .scope(dto.getScope())
                .dcqlQuery(dto.getDcqlQuery())
                .description(dto.getDescription())
                .enabled(dto.getEnabled() != null ? dto.getEnabled() : true)
                .build();

        dcqlScopeMappingRepository.save(entity);
        reloadSdkCache();
    }

    public DcqlScopeMappingDTO updateScopeMapping(Long id, DcqlScopeMappingDTO dto) {
        DcqlScopeMapping entity = dcqlScopeMappingRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND));

        entity.setScope(dto.getScope());
        entity.setDcqlQuery(dto.getDcqlQuery());
        entity.setDescription(dto.getDescription());
        if (dto.getEnabled() != null) {
            entity.setEnabled(dto.getEnabled());
        }

        DcqlScopeMapping saved = dcqlScopeMappingRepository.save(entity);
        reloadSdkCache();
        return DcqlScopeMappingDTO.toDTO(saved);
    }

    public void deleteScopeMapping(Long id) {
        DcqlScopeMapping entity = dcqlScopeMappingRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND));
        dcqlScopeMappingRepository.delete(entity);
        reloadSdkCache();
    }

    public List<DcqlScopeMappingDTO> searchPopupList(String searchValue) {
        List<DcqlScopeMapping> all = dcqlScopeMappingRepository.findAllByEnabledTrue();
        if (searchValue == null || "all".equalsIgnoreCase(searchValue)) {
            return all.stream().map(DcqlScopeMappingDTO::toDTO).toList();
        }
        return all.stream()
                .filter(m -> m.getScope().toLowerCase().contains(searchValue.toLowerCase())
                        || (m.getDescription() != null && m.getDescription().toLowerCase().contains(searchValue.toLowerCase())))
                .map(DcqlScopeMappingDTO::toDTO)
                .toList();
    }

    public List<ListCredentialSchemaDto> getTasCredentialSchemas() {
        return listFeign.requestCredentialSchemaList();
    }

    private void reloadSdkCache() {
        try {
            scopeToDCQLMapperService.reloadMappings();
            log.info("SDK DCQL scope mapping cache reloaded");
        } catch (Exception e) {
            log.warn("Failed to reload SDK DCQL scope mapping cache: {}", e.getMessage());
        }
    }
}
