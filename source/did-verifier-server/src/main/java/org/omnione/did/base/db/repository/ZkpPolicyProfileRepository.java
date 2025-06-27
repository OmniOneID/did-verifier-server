package org.omnione.did.base.db.repository;

import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.omnione.did.base.db.repository.projection.ZkpProofRequestIdProjection;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;

public interface ZkpPolicyProfileRepository extends JpaRepository<ZkpPolicyProfile, Long>, QuerydslPredicateExecutor<ZkpPolicyProfile>, ZkpPolicyProfileRepositoryAdmin {
//    ZkpPolicyProfile findByPolicyId(String policyId);
//
//    ZkpPolicyProfile findByPayloadId(String payloadId);
//
    Optional<ZkpPolicyProfile> findByProfileId(String profileId);
    Optional<ZkpPolicyProfile> findByZkpProofRequestId(Long zkpProofRequestId);

    @Query("SELECT a.zkpProofRequestId AS zkpProofRequestId, COUNT(a) AS count " +
            "FROM ZkpPolicyProfile a " +
            "WHERE a.zkpProofRequestId IN :zkpProofRequestIds " +
            "GROUP BY a.zkpProofRequestId")
    List<ZkpProofRequestIdProjection> countByZkpProofRequestIdIn(List<Long> zkpProofRequestIds);
    List<ZkpPolicyProfile> findByTitleContainingIgnoreCase(String title, Sort sort);
}
