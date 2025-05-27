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
package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.repository.projection.PayloadIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VpPolicy entity operations.
 * Provides CRUD operations for VpPolicy entities and custom query methods.
 *
 */
public interface PolicyRepository extends JpaRepository<Policy, Long>, QuerydslPredicateExecutor<Policy>, PolicyRepositoryAdmin {

    Optional<Policy> findByPolicyId(String policyId);

    List<Policy> findByPayloadId(String payloadId);

    List<Policy> findByPolicyProfileId(String policyProfileId);

    @Query("SELECT p.payloadId AS payloadId, COUNT(p) AS count " +
            "FROM Policy p " +
            "WHERE p.payloadId IN :payloadIds " +
            "GROUP BY p.payloadId")
    List<PayloadIdProjection> countByPayloadIdIn(@Param("payloadIds") List<String> payloadIds);

    long countByPayloadId(String payloadId);

    @Query("SELECT p.policyProfileId AS policyProfileId, COUNT(p) AS count " +
            "FROM Policy p " +
            "WHERE p.policyProfileId IN :policyProfileIds " +
            "GROUP BY p.policyProfileId")
    List<PolicyProfileIdProjection> countByPolicyProfileIdIn(@Param("policyProfileIds") List<String> policyProfileIds);
}
