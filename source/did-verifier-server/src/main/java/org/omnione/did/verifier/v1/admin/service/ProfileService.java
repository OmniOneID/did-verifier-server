package org.omnione.did.verifier.v1.admin.service;

import org.omnione.did.verifier.v1.admin.dto.ProfileDTO;

import java.util.List;

/**
 * The ProfileService interface provides methods for querying the database for profiles.
 * It is designed to facilitate the retrieval of profiles from the database, ensuring that the data is accurate and up-to-date.
 */
public interface ProfileService {
    List<ProfileDTO> getProfileList(String name);

    ProfileDTO getProfileInfo(long profileId);

    void saveProfile(ProfileDTO profileDTO);

    ProfileDTO updateProfile(ProfileDTO profileDTO);

    void deleteProfile(long profileId);
}
