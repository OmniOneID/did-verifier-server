package org.omnione.did.base.db.repository;


import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ZkpProofRequestRepository extends JpaRepository<ZkpProofRequest, Long>
{
    ZkpProofRequest findById(long id);
//
//    ZkpProofRequest findByPolicyId(String policyId);
//
//    ZkpProofRequest findByPayloadId(String payloadId);
//
//    ZkpProofRequest findByPolicyProfileId(String policyProfileId);
}
