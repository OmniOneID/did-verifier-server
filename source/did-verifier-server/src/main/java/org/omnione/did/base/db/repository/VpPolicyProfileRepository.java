package org.omnione.did.base.db.repository;
import org.omnione.did.base.db.domain.VpPolicyProfile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * The VpPolicyProfileRepository interface provides methods for querying the database for VP policy profiles.
 * It is designed to facilitate the retrieval of VP policy profiles from the database, ensuring that the data is accurate and up-to-date.
 */
public interface VpPolicyProfileRepository extends JpaRepository<VpPolicyProfile, Long> {
//    Optional<VpPolicyProfile> findByPolicyId(String policyId);
//    Optional<VpPolicyProfile> findByPayloadId(String payloadId);
//    Optional<VpPolicyProfile> findByPolicyIdAndPayloadId(String policyId, String payloadId);
//    List<VpPolicyProfile> findByPolicyIdIn(List<String> policyIds);
//    List<VpPolicyProfile> findByPayloadIdIn(List<String> payloadIds);
    List<VpPolicyProfile> findByTitle(String title, Sort sort);
    Optional<VpPolicyProfile> findByPolicyProfileId(String policyProfileId);
}
