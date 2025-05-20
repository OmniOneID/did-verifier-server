package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZkpPolicyProfileRepository extends JpaRepository<ZkpPolicyProfile, Long> {
//    ZkpPolicyProfile findByPolicyId(String policyId);
//
//    ZkpPolicyProfile findByPayloadId(String payloadId);
//
    Optional<ZkpPolicyProfile> findByProfileId(String profileId);
    Optional<ZkpPolicyProfile> findByZkpProofRequestId(Long zkpProofRequestId);
}
