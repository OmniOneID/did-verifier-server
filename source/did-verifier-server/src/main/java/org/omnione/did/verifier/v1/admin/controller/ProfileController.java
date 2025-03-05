package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.ProfileDTO;
import org.omnione.did.verifier.v1.admin.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * The AdminController class provides methods for saving and getting verifier information.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get Profile List", description = "Get a list of profiles by name (optional).")
    @GetMapping(UrlConstant.Verifier.GET_PROFILE_LIST)
    public List<ProfileDTO> getProfileList(@RequestParam(required = false) String name) {
        return profileService.getProfileList(name);
    }

    @Operation(summary = "Get Profile Info", description = "Get a single profile's information.")
    @GetMapping(UrlConstant.Verifier.GET_PROFILE_INFO)
    public ProfileDTO getProfileInfo(@RequestParam long profileId) {
        return profileService.getProfileInfo(profileId);
    }

    @Operation(summary = "Save Profile", description = "Save a new profile.")
    @PostMapping(UrlConstant.Verifier.SAVE_PROFILE_INFO)
    public ResponseEntity<Void> saveProfile(@RequestBody ProfileDTO profileDTO) {
        profileService.saveProfile(profileDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update Profile", description = "Update an existing profile.")
    @PutMapping(UrlConstant.Verifier.UPDATE_PROFILE_INFO)
    public ResponseEntity<ProfileDTO> updateProfile(@RequestBody ProfileDTO profileDTO) {
        log.info("Updating Profile: {}", profileDTO);
        ProfileDTO updatedProfileDTO = profileService.updateProfile(profileDTO);
        return ResponseEntity.ok(updatedProfileDTO);
    }

    @Operation(summary = "Delete Profile", description = "Delete a profile by ID.")
    @DeleteMapping(UrlConstant.Verifier.DELETE_PROFILE_INFO)
    public ResponseEntity<Void> deleteProfile(@RequestParam long profileId) {
        profileService.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }

}
