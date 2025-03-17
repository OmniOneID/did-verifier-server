package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.PolicyDTO;
import org.omnione.did.verifier.v1.admin.service.PolicyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The PolicyController class provides methods for managing policies in the DID Verifier application.
 * It is designed to facilitate the retrieval, creation, updating, and deletion of policies in the application.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "Get Policy List", description = "Get a list of policies by title (optional).")
    @GetMapping(UrlConstant.Verifier.GET_POLICY_LIST)
    public List<PolicyDTO> getPolicyList() {
        return policyService.getPolicyList();
    }

    @Operation(summary = "Get Policy Info", description = "Get a single policy's information.")
    @GetMapping(UrlConstant.Verifier.GET_POLICY_INFO)
    public PolicyDTO getPolicyInfo(@PathVariable Long id) {
        return policyService.getPolicyInfo(id);
    }

    @Operation(summary = "Save Policy", description = "Save a new policy.")
    @PostMapping(UrlConstant.Verifier.SAVE_POLICY_INFO)
    public ResponseEntity<Void> savePolicy(@RequestBody PolicyDTO policyDTO) {
        policyService.savePolicy(policyDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update Policy", description = "Update an existing policy.")
    @PutMapping(UrlConstant.Verifier.UPDATE_POLICY_INFO)
    public ResponseEntity<PolicyDTO> updatePolicy(@RequestBody PolicyDTO policyDTO) {
        log.info("Updating Policy: {}", policyDTO);
        PolicyDTO updatedPolicyDTO = policyService.updatePolicy(policyDTO);
        return ResponseEntity.ok(updatedPolicyDTO);
    }

    @Operation(summary = "Delete Policy", description = "Delete a policy by ID.")
    @DeleteMapping(UrlConstant.Verifier.DELETE_POLICY_INFO)
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }



}
