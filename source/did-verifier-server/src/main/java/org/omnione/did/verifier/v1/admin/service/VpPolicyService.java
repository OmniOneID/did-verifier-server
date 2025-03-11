package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The VpPolicyServiceImpl class provides methods for querying the database for VP policies.
 * It is designed to facilitate the retrieval of VP policies from the database, ensuring that the data is accurate and up-to-date.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VpPolicyService  {
    private final PolicyRepository policyRepository;
    private final ModelMapper modelMapper;


    public List<PolicyDTO> getPolicyList() {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        List<Policy> policyList = policyRepository.findAll(sort);
        return policyList.stream().map(p -> modelMapper.map(p, PolicyDTO.class)).collect(Collectors.toList());

    }


    public PolicyDTO getPolicyInfo(String policyId) {
        Policy policy = policyRepository.findByPolicyId(policyId).orElse(null);
        return PolicyDTO.toDTO(policy);
    }


    public void savePolicy(PolicyDTO policyDTO) {
        policyRepository.save(modelMapper.map(policyDTO, Policy.class));
    }


    public PolicyDTO updatePolicy(PolicyDTO policyDTO) {
        Policy findPolicy = policyRepository.findByPolicyId(policyDTO.getPolicyId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));

            findPolicy.setPayloadId(policyDTO.getPayloadId());
            findPolicy.setProfileId(policyDTO.getProfileId());
        try {
            Policy savedPolicy = policyRepository.save(findPolicy);
            return PolicyDTO.toDTO(savedPolicy);
        } catch (Exception e) {
            log.error("Failed to update policy: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.VP_POLICY_UPDATE_FAILED);

        }

    }

    public void deletePolicy(String policyId) {
        Policy policy = policyRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));
        policyRepository.delete(policy);
    }
}
