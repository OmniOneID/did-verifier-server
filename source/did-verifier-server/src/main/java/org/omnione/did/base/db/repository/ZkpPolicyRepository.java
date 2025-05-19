package org.omnione.did.base.db.repository;


import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.ZkpPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VpPolicy entity operations.
 * Provides CRUD operations for VpPolicy entities and custom query methods.
 *
 */
public interface ZkpPolicyRepository extends JpaRepository<ZkpPolicy, Long> {

    Optional<ZkpPolicy> findByPolicyId(String policyId);

    //List<ZkpPolicy> findByPayloadId(String payloadId);

    //Optional<ZkpPolicy> findByPolicyProfileId(String policyProfileId);
}
