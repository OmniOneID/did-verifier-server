package org.omnione.did.base.db.repository;


import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.repository.projection.PayloadIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VpPolicy entity operations.
 * Provides CRUD operations for VpPolicy entities and custom query methods.
 *
 */
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyId(String policyId);

    List<Policy> findByPayloadId(String payloadId);

    List<Policy> findByPolicyProfileId(String policyProfileId);

    @Query("SELECT p.payloadId AS payloadId, COUNT(p) AS count " +
            "FROM Policy p " +
            "WHERE p.payloadId IN :payloadIds " +
            "GROUP BY p.payloadId")
    List<PayloadIdProjection> countByPayloadIdIn(@Param("payloadIds") List<String> payloadIds);

    long countByPayloadId(String payloadId);
}
