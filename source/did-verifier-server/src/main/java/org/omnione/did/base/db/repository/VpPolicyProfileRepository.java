package org.omnione.did.base.db.repository;

import java.util.List;

/**
 * Description...
 *
 * @author : jinhwan-notebook
 * @fileName : VpPolicyProfileRepository
 * @since : 2025. 2. 24.
 */
public interface VpPolicyProfileRepository extends JpaRepository<VpPolicyProfile, Long> {
    Optional<VpPolicyProfile> findByPolicyId(String policyId);
    Optional<VpPolicyProfile> findByPayloadId(String payloadId);
    Optional<VpPolicyProfile> findByPolicyIdAndPayloadId(String policyId, String payloadId);
    List<VpPolicyProfile> findByPolicyIdIn(List<String> policyIds);
    List<VpPolicyProfile> findByPayloadIdIn(List<String> payloadIds);

}
