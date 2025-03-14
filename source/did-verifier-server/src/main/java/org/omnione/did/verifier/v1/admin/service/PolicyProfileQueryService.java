package org.omnione.did.verifier.v1.admin.service;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.VpProfileRepository;
import org.omnione.did.verifier.v1.admin.dto.PolicyProfileDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PolicyProfileQueryService {
    private final PolicyProfileRepository policyProfileRepository;

    public Page<PolicyProfileDTO> searchPolicyProfileList(String searchKey, String searchValue, Pageable pageable) {
        Page<PolicyProfile> policyProfileList = policyProfileRepository.searchPolicyProfileList(searchKey, searchValue, pageable);

        List<PolicyProfileDTO> policyProfileDTOS = policyProfileList.getContent().stream()
                .map(PolicyProfileDTO::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(policyProfileDTOS, pageable, policyProfileList.getTotalElements());
    }

}
