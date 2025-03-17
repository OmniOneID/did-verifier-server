package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.PolicyProfileDTO;
import org.omnione.did.verifier.v1.admin.service.PolicyProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class PolicyProfileController {

    private final PolicyProfileService policyProfileService;

@Operation(summary = "Get Profile List", description = "Get a list of profiles by name (optional).")
@GetMapping(UrlConstant.Verifier.GET_PROFILE_LIST)
public Page<PolicyProfileDTO> searchPolicyProfileList(String searchKey, String searchValue, Pageable pageable) {
    return policyProfileService.searchPolicyProfileList(searchKey, searchValue, pageable);
}

    @Operation(summary = "Get Profile Info", description = "Get a single profile's information.")
    @GetMapping(UrlConstant.Verifier.GET_PROFILE_INFO)
    public PolicyProfileDTO getProfileInfo(@PathVariable Long id) {
        return policyProfileService.getProfileInfo(id);
    }

    @Operation(summary = "Save Profile", description = "Save a new profile.")
    @PostMapping(UrlConstant.Verifier.SAVE_PROFILE_INFO)
    public ResponseEntity<Void> saveProfile(@RequestBody PolicyProfileDTO policyProfileDTO) {
        policyProfileService.saveProfile(policyProfileDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update Profile", description = "Update an existing profile.")
    @PutMapping(UrlConstant.Verifier.UPDATE_PROFILE_INFO)
    public ResponseEntity<PolicyProfileDTO> updateProfile(@RequestBody PolicyProfileDTO policyProfileDTO) {
        log.info("Updating Profile: {}", policyProfileDTO);
        PolicyProfileDTO updatedPolicyProfileDTO = policyProfileService.updateProfile(policyProfileDTO);
        return ResponseEntity.ok(updatedPolicyProfileDTO);
    }

    @Operation(summary = "Delete Profile", description = "Delete a profile by ID.")
    @DeleteMapping(UrlConstant.Verifier.DELETE_PROFILE_INFO)
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        policyProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Search Profile List", description = "Search a list of profiles by name (optional).")
    @GetMapping(UrlConstant.Verifier.GET_POPUP_PROFILE_LIST)
    public List<PolicyProfileDTO> getProfileList(@PathVariable String searchValue) {
        return policyProfileService.getProfileList(searchValue);
    }

}
