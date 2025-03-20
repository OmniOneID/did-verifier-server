package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.PolicyProfile;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.base.db.repository.PolicyProfileRepository;
import org.omnione.did.base.db.repository.VpProcessRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.omnione.did.verifier.v1.admin.dto.PolicyProfileDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PolicyProfileService implementation for managing profiles.
 */
@RequiredArgsConstructor
@Service
public class PolicyProfileService {
    private final PolicyProfileRepository profileRepository;
    private final VpFilterRepository vpFilterRepository;
    private final VpProcessRepository vpProcessRepository;
    private final VerifierProperty verifierConfig;
    private final ModelMapper modelMapper;
    private final PolicyProfileQueryService policyProfileQueryService;
    private final PolicyRepository policyRepository;


    public List<PolicyProfileDTO> getProfileList(String title) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));

        List<PolicyProfile> policyProfileList;
        if(Objects.equals(title, "all")) {
            policyProfileList = profileRepository.findAll(sort);
        } else {
            policyProfileList = profileRepository.findByTitle(title, sort);
        }

        return policyProfileList.stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    public PolicyProfileDTO getProfileInfo(long profileId) {
        PolicyProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return convertToProfileDTO(profile);
    }

    @Transactional
    public void saveProfile(PolicyProfileDTO policyProfileDTO) {
        PolicyProfile profile = policyProfileDTO.makeEntity();
        profileRepository.save(profile);
    }

    @Transactional
    public PolicyProfileDTO updateProfile(PolicyProfileDTO policyProfileDTO) {
        PolicyProfile existingProfile = profileRepository.findById(policyProfileDTO.getId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));

        // Update existing profile with new values
        existingProfile.setTitle(policyProfileDTO.getTitle());
        existingProfile.setDescription(policyProfileDTO.getDescription());
        existingProfile.setEncoding(policyProfileDTO.getEncoding());
        existingProfile.setLanguage(policyProfileDTO.getLanguage());
        existingProfile.setProcessId(policyProfileDTO.getProcessId());
        existingProfile.setFilterId(policyProfileDTO.getFilterId());
        existingProfile.setType(policyProfileDTO.getType());
        if (policyProfileDTO.getLogo() != null) {
            existingProfile.setFormat(policyProfileDTO.getLogo().getFormat());
            existingProfile.setLink(policyProfileDTO.getLogo().getLink());
            existingProfile.setValue(policyProfileDTO.getLogo().getValue());
        }

        return modelMapper.map(profileRepository.save(existingProfile), PolicyProfileDTO.class);
    }

    @Transactional
    public void deleteProfile(long profileId) {
        PolicyProfile policyProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));

        // Check if profile is referenced in any policy
        List<Policy> referencingPolicies = policyRepository.findByPolicyProfileId(policyProfile.getPolicyProfileId());
        if (!referencingPolicies.isEmpty()) {
            throw new OpenDidException(ErrorCode.VP_POLICY_PROFILE_IN_USE);
        }

        profileRepository.delete(policyProfile);
    }

    /**
     * Convert PolicyProfile entity to ProfileDTO with filter, process, and verifier details.
     */
    private PolicyProfileDTO convertToProfileDTO(PolicyProfile profile) {
        String filterTitle = vpFilterRepository.findByFilterId(profile.getFilterId())
                .map(VpFilter::getTitle)
                .orElse("Empty Filter");
        String processTitle = vpProcessRepository.findById(profile.getProcessId())
                .map(VpProcess::getTitle)
                .orElse("Empty Process");

        PolicyProfileDTO.Verifier verifier = new PolicyProfileDTO.Verifier();
        verifier.setDid(verifierConfig.getDid());
        verifier.setCertVcRef(verifierConfig.getCertVcRef());
        verifier.setName(verifierConfig.getName());
        verifier.setRef(verifierConfig.getRef());

        PolicyProfileDTO.LogoImage logoImage = null;
        if (profile.getFormat() != null) {
            logoImage = new PolicyProfileDTO.LogoImage(
                    profile.getFormat(),
                    profile.getLink(),
                    profile.getValue()
            );
        }
        return PolicyProfileDTO.builder()
                .id(profile.getId())
                .policyProfileId(profile.getPolicyProfileId())
                .type(profile.getType())
                .title(profile.getTitle())
                .description(profile.getDescription())
                .logo(logoImage)
                .encoding(profile.getEncoding())
                .language(profile.getLanguage())
                .processId(profile.getProcessId())
                .filterId(profile.getFilterId())
                .filterTitle(filterTitle)
                .processTitle(processTitle)
                .verifier(verifier)
                .build();
    }

    public Page<PolicyProfileDTO> searchPolicyProfileList(String searchKey, String searchValue, Pageable pageable) {
        return policyProfileQueryService.searchPolicyProfileList(searchKey, searchValue, pageable);
    }
}
