package org.omnione.did.base.db.repository;


import org.omnione.did.base.db.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VpPolicy entity operations.
 * Provides CRUD operations for VpPolicy entities and custom query methods.
 *
 */
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyId(String policyId);
}
