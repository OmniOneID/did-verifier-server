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
import org.omnione.did.base.db.domain.Oid4vpConfig;
import org.omnione.did.base.db.repository.Oid4vpConfigRepository;
import org.omnione.did.oid4vc.oid4vp.dto.OID4VPConfigDto;
import org.omnione.did.oid4vc.oid4vp.repository.OID4VPRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * JPA-based implementation of the SDK OID4VPRepository.
 * Reads from the database instead of the in-memory store and supplies the data to the SDK VerifierConfigService.
 */
@Slf4j
@RequiredArgsConstructor
public class JpaOid4vpRepositoryAdapter implements OID4VPRepository {

    private final Oid4vpConfigRepository jpaRepository;

    @Override
    public Optional<OID4VPConfigDto> findByType(String type) {
        return jpaRepository.findByType(type).map(this::toDto);
    }

    @Override
    public Optional<OID4VPConfigDto> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<OID4VPConfigDto> findAll() {
        return jpaRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public OID4VPConfigDto save(OID4VPConfigDto dto) {
        Oid4vpConfig entity = jpaRepository.findById(dto.getId() != null ? dto.getId() : 0L)
                .orElse(new Oid4vpConfig());

        entity.setType(dto.getType());
        entity.setConfig(dto.getConfig());

        Oid4vpConfig saved = jpaRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByType(String type) {
        return jpaRepository.findByType(type).isPresent();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    private OID4VPConfigDto toDto(Oid4vpConfig entity) {
        return OID4VPConfigDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .config(entity.getConfig())
                .createdAt(entity.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneId.systemDefault()) : null)
                .updatedAt(entity.getUpdatedAt() != null
                        ? LocalDateTime.ofInstant(entity.getUpdatedAt(), ZoneId.systemDefault()) : null)
                .build();
    }
}
