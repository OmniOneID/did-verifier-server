package org.omnione.did.base.db.repository;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.VpProfile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

/**
 * The PolicyProfileRepository interface provides methods for querying the database for policy profiles.
 * It is designed to facilitate the retrieval of VP policy profiles from the database, ensuring that the data is accurate and up-to-date.
 */
public interface PolicyProfileRepository extends JpaRepository<PolicyProfile, Long>, QuerydslPredicateExecutor<VpProfile>, PolicyProfileRepositoryAdmin {

    List<PolicyProfile> findByTitle(String title, Sort sort);
    Optional<PolicyProfile> findByPolicyProfileId(String policyProfileId);
}
