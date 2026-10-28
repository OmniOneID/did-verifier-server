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

package org.omnione.did.verifier.v1.oid4vp.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.DcqlScopeMapping;
import org.omnione.did.oid4vc.oid4vp.dto.DCQLScopeMappingDto;
import org.omnione.did.oid4vc.oid4vp.repository.DCQLScopeMappingRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * JPA-based implementation of the SDK DCQLScopeMappingRepository.
 * Queries the database directly instead of using the in-memory store.
 */
@Slf4j
@RequiredArgsConstructor
public class JpaDcqlScopeMappingRepositoryAdapter implements DCQLScopeMappingRepository {

    private final org.omnione.did.base.db.repository.DcqlScopeMappingRepository jpaRepository;

    @Override
    public Optional<DCQLScopeMappingDto> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDto);
    }

    @Override
    public Optional<DCQLScopeMappingDto> findByScope(String scope) {
        return jpaRepository.findByScope(scope).map(this::toDto);
    }

    @Override
    public List<DCQLScopeMappingDto> findAllEnabled() {
        return jpaRepository.findAllByEnabledTrue().stream().map(this::toDto).toList();
    }

    @Override
    public List<DCQLScopeMappingDto> findAll() {
        return jpaRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public DCQLScopeMappingDto save(DCQLScopeMappingDto dto) {
        DcqlScopeMapping entity = dto.getId() != null
                ? jpaRepository.findById(dto.getId()).orElse(new DcqlScopeMapping())
                : new DcqlScopeMapping();

        entity.setScope(dto.getScope());
        entity.setDcqlQuery(dto.getDcqlQuery());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(dto.getEnabled());

        DcqlScopeMapping saved = jpaRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByScope(String scope) {
        jpaRepository.findByScope(scope).ifPresent(entity -> jpaRepository.deleteById(entity.getId()));
    }

    @Override
    public boolean existsByScope(String scope) {
        return jpaRepository.findByScope(scope).isPresent();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    private DCQLScopeMappingDto toDto(DcqlScopeMapping entity) {
        return DCQLScopeMappingDto.builder()
                .id(entity.getId())
                .scope(entity.getScope())
                .dcqlQuery(entity.getDcqlQuery())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneId.systemDefault()) : null)
                .updatedAt(entity.getUpdatedAt() != null
                        ? LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneId.systemDefault()) : null)
                .build();
    }
}
