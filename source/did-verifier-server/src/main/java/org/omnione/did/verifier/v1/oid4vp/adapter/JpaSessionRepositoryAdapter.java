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
import org.omnione.did.base.db.domain.Oid4vpSession;
import org.omnione.did.base.db.repository.Oid4vpSessionJpaRepository;
import org.omnione.did.oid4vc.oid4vp.dto.VerificationSession;
import org.omnione.did.oid4vc.oid4vp.repository.SessionRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JPA-based implementation of the SDK SessionRepository.
 * Stores sessions in the database (oid4vp_session) instead of in memory, so they survive server restarts.
 */
@Slf4j
@RequiredArgsConstructor
public class JpaSessionRepositoryAdapter implements SessionRepository {

    private final Oid4vpSessionJpaRepository jpaRepository;

    @Override
    public Optional<VerificationSession> findByState(String state) {
        return jpaRepository.findByState(state).map(this::toDto);
    }

    @Override
    public Optional<VerificationSession> findByRequestId(String requestId) {
        return jpaRepository.findByRequestId(requestId).map(this::toDto);
    }

    @Override
    public Optional<VerificationSession> findByTransactionId(String transactionId) {
        return jpaRepository.findByTransactionId(transactionId).map(this::toDto);
    }

    @Override
    public Map<String, VerificationSession> findAll() {
        Map<String, VerificationSession> result = new LinkedHashMap<>();
        jpaRepository.findAll().forEach(entity ->
                result.put(entity.getState(), toDto(entity)));
        return result;
    }

    @Override
    public void saveByState(String state, VerificationSession session) {
        Oid4vpSession entity = jpaRepository.findByState(state)
                .orElse(new Oid4vpSession());

        entity.setTransactionId(session.getTransactionId());
        entity.setState(state);
        entity.setNonce(session.getNonce());
        entity.setDcqlQuery(session.getDcqlQuery());
        entity.setResponseMode(session.getResponseMode());
        entity.setRequestId(session.getRequestId());
        entity.setStatus(session.getStatus());
        entity.setClientMetadata(session.getClientMetadata());
        entity.setRequestUriFetchedAt(session.getRequestUriFetchedAt());
        entity.setVpToken(session.getVpToken());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setExpiresAt(session.getExpiresAt());
        entity.setUpdatedAt(session.getUpdatedAt());

        jpaRepository.save(entity);
    }

    @Override
    public boolean existsByState(String state) {
        return jpaRepository.findByState(state).isPresent();
    }

    @Override
    public void clear() {
        jpaRepository.deleteAll();
    }

    @Override
    public int count() {
        return (int) jpaRepository.count();
    }

    private VerificationSession toDto(Oid4vpSession entity) {
        VerificationSession session = new VerificationSession();
        session.setTransactionId(entity.getTransactionId());
        session.setState(entity.getState());
        session.setNonce(entity.getNonce());
        session.setDcqlQuery(entity.getDcqlQuery());
        session.setResponseMode(entity.getResponseMode());
        session.setRequestId(entity.getRequestId());
        session.setStatus(entity.getStatus());
        session.setClientMetadata(entity.getClientMetadata());
        session.setRequestUriFetchedAt(entity.getRequestUriFetchedAt());
        session.setVpToken(entity.getVpToken());
        session.setCreatedAt(entity.getCreatedAt());
        session.setExpiresAt(entity.getExpiresAt());
        session.setUpdatedAt(entity.getUpdatedAt());
        return session;
    }
}
