package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.omnione.did.base.db.domain.VpFilter;
import org.omnione.did.base.db.domain.VpPolicyProfile;
import org.omnione.did.base.db.domain.VpProcess;
import org.omnione.did.base.db.repository.VpFilterRepository;
import org.omnione.did.base.db.repository.VpPolicyProfileRepository;
import org.omnione.did.base.db.repository.VpProcessRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.omnione.did.verifier.v1.admin.dto.ProfileDTO;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProfileService implementation for managing profiles.
 */
@RequiredArgsConstructor
@Service
public class ProfileService {
    private final VpPolicyProfileRepository profileRepository;
    private final VpFilterRepository vpFilterRepository;
    private final VpProcessRepository vpProcessRepository;
    private final VerifierProperty verifierConfig; // Verifier 설정 주입
    private final ModelMapper modelMapper;

    public List<ProfileDTO> getProfileList(String title) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));

        List<VpPolicyProfile> vpPolicyProfileList =
                (title != null && !title.isEmpty())
                        ? profileRepository.findByTitle(title, sort)
                        : profileRepository.findAll(sort);

        return vpPolicyProfileList.stream()
                .map(this::convertToProfileDTO)
                .collect(Collectors.toList());
    }

    public ProfileDTO getProfileInfo(long profileId) {
        VpPolicyProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return convertToProfileDTO(profile);
    }

    @Transactional
    public void saveProfile(ProfileDTO profileDTO) {
        VpPolicyProfile profile = profileDTO.toEntity();
        profileRepository.save(profile);
    }

    @Transactional
    public ProfileDTO updateProfile(ProfileDTO profileDTO) {
        VpPolicyProfile existingProfile = profileRepository.findById(profileDTO.getId())
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));

        // Update existing profile with new values
        existingProfile.setTitle(profileDTO.getTitle());
        existingProfile.setDescription(profileDTO.getDescription());
        existingProfile.setEncoding(profileDTO.getEncoding());
        existingProfile.setLanguage(profileDTO.getLanguage());
        existingProfile.setProcessId(profileDTO.getProcessId());
        existingProfile.setFilterId(profileDTO.getFilterId());
        existingProfile.setType(profileDTO.getType());

        return modelMapper.map(profileRepository.save(existingProfile), ProfileDTO.class);
    }

    @Transactional
    public void deleteProfile(long profileId) {
        VpPolicyProfile vpPolicyProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));
        profileRepository.delete(vpPolicyProfile);
    }

    /**
     * Convert VpPolicyProfile entity to ProfileDTO with filter, process, and verifier details.
     */
    private ProfileDTO convertToProfileDTO(VpPolicyProfile profile) {
        String filterTitle = vpFilterRepository.findByFilterId(profile.getFilterId())
                .map(VpFilter::getTitle)
                .orElse("Unknown Filter");
        String processTitle = vpProcessRepository.findById(profile.getProcessId())
                .map(VpProcess::getTitle)
                .orElse("Unknown Process");

        ProfileDTO.Verifier verifier = new ProfileDTO.Verifier();
        verifier.setDid(verifierConfig.getDid());
        verifier.setCertVcRef(verifierConfig.getCertVcRef());
        verifier.setName(verifierConfig.getName());
        verifier.setRef(verifierConfig.getRef());

        ProfileDTO.LogoImage logoImage = null;
        if (profile.getFormat() != null) {
            logoImage = new ProfileDTO.LogoImage(
                    profile.getFormat(),
                    profile.getLink(),
                    profile.getValue()
            );
        }
        return ProfileDTO.builder()
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
}
