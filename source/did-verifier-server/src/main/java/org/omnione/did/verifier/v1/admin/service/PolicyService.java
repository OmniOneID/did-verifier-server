package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;
import org.omnione.did.verifier.v1.admin.dto.VpSubmitDTO;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The VpPolicyServiceImpl class provides methods for querying the database for VP policies.
 * It is designed to facilitate the retrieval of VP policies from the database, ensuring that the data is accurate and up-to-date.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final PayloadRepository payloadRepository;
    private final PolicyProfileRepository policyProfileRepository;


    public List<PolicyDTO> getPolicyList() {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        List<Policy> policyList = policyRepository.findAll(sort);
        return policyList.stream()
                .map(this::convertToPolicyDTO)
                .collect(Collectors.toList());
    }


    public PolicyDTO getPolicyInfo(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));
        return convertToPolicyDTO(policy);
    }


    public void savePolicy(PolicyDTO policyDTO) {
        Policy policy = Policy.builder()
                .policyId(UUID.randomUUID().toString())
                .payloadId(policyDTO.getPayloadId())
                .policyProfileId(policyDTO.getPolicyProfileId())
                .policyTitle(policyDTO.getPolicyTitle())
                .build();

        policyRepository.save(policy);
    }


    public PolicyDTO updatePolicy(PolicyDTO policyDTO) {
        Policy findPolicy = policyRepository.findById(policyDTO.getId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));
            findPolicy.setPayloadId(policyDTO.getPayloadId());
            findPolicy.setPolicyProfileId(policyDTO.getPolicyProfileId());
            Policy savedPolicy = policyRepository.save(findPolicy);
            return PolicyDTO.toDTO(savedPolicy);
    }

    public void deletePolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));
        policyRepository.delete(policy);
    }

    private PolicyDTO convertToPolicyDTO(Policy policy) {
        String payloadService = payloadRepository.findByPayloadId(policy.getPayloadId())
                .map(Payload::getService)
                .orElse("Unknown Payload Service");
        String profileTitle = policyProfileRepository.findByPolicyProfileId(policy.getPolicyProfileId())
                .map(PolicyProfile::getTitle)
                .orElse("Unknown Profile Title");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return PolicyDTO.builder()
                .id(policy.getId())
                .policyId(policy.getPolicyId())
                .payloadId(policy.getPayloadId())
                .policyProfileId(policy.getPolicyProfileId())
                .policyTitle(policy.getPolicyTitle())
                .payloadService(payloadService)
                .profileTitle(profileTitle)
                .createdAt(formatInstant(policy.getCreatedAt(), formatter))
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        return VpSubmitDTO.formatInstant(instant, formatter);
    }
}
