package org.omnione.did.verifier.v1.admin.service;

import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;

import java.util.List;

/**
 * The VpPolicyService interface provides methods for querying the database for VP policies.
 * It is designed to facilitate the retrieval of VP policies from the database, ensuring that the data is accurate and up-to-date.
 */
public interface VpPolicyService {
    List<PolicyDTO> getPolicyList();

    PolicyDTO getPolicyInfo(String policyId);

    void savePolicy(PolicyDTO policyDTO);

    PolicyDTO updatePolicy(PolicyDTO policyDTO);

    void deletePolicy(String policyId);
}
