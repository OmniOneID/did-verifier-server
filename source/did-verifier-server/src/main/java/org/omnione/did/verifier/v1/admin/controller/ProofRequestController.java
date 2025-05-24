/*
 * Copyright 2025 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnione.did.verifier.v1.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.base.constants.UrlConstant.Verifier;
import org.omnione.did.verifier.v1.admin.api.dto.ListCredentialSchemaDto;
import org.omnione.did.verifier.v1.admin.dto.ProofRequestDto;
import org.omnione.did.verifier.v1.admin.dto.VerifyUniqueResDto;
import org.omnione.did.verifier.v1.admin.dto.ZkpProofRequestDto;
import org.omnione.did.verifier.v1.admin.service.ProofRequestService;
import org.omnione.did.verifier.v1.common.dto.EmptyResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class ProofRequestController {
    private final ProofRequestService proofRequestService;

    @Operation(summary = "Get Proof Request List", description = "Get a list of proof request.")
    @GetMapping(UrlConstant.Verifier.GET_PROOF_REQUEST)
    public Page<ZkpProofRequestDto> searchProofRequestList(String searchKey, String searchValue, Pageable pageable) {
        return proofRequestService.searchProofRequestList(searchKey, searchValue, pageable);
    }

    @Operation(summary = "Get Credential Schema List", description = "Get a list of credential request.")
    @GetMapping(Verifier.GET_CREDENTIAL_SCHEMA_LIST)
    public ResponseEntity<List<ListCredentialSchemaDto>> getCredentialSchemaList() {
        List<ListCredentialSchemaDto> credentialSchemaList = proofRequestService.getCredentialSchemaList();
        return ResponseEntity.ok(credentialSchemaList);
    }

    @Operation(summary = "Save Proof Request", description = "Save a new proof request")
    @PostMapping(UrlConstant.Verifier.SAVE_PROOF_REQUEST)
    public ResponseEntity<EmptyResDto> createProofRequest(@RequestBody ProofRequestDto request) {
        return ResponseEntity.ok(proofRequestService.createProofRequest(request));
    }

    @Operation(summary = "Check Proof Request name", description = "Check name of proof request")
    @GetMapping(UrlConstant.Verifier.CHECK_PROOF_REQUEST_NAME)
    public ResponseEntity<VerifyUniqueResDto> verifyNameUnique(@RequestParam String name) {
        return ResponseEntity.ok(proofRequestService.verifyNameUnique(name));
    }

    @Operation(summary = "Get Proof Request Info", description = "Get Proof Request Info")
    @GetMapping(UrlConstant.Verifier.GET_PROOF_REQUEST_INFO)
    public ResponseEntity<ProofRequestDto> getProofRequestInfo(@PathVariable Long id) {
        return ResponseEntity.ok(proofRequestService.getProofRequestInfo(id));
    }

    @Operation(summary = "Update Proof Request", description = "Update a proof request")
    @PutMapping(UrlConstant.Verifier.UPDATE_PROOF_REQUEST)
    public ResponseEntity<EmptyResDto> updateProofRequest(@RequestBody ProofRequestDto request) {
        return ResponseEntity.ok(proofRequestService.updateProofRequest(request));
    }

    @Operation(summary = "Delete Proof Request", description = "Delete a proof request")
    @DeleteMapping(UrlConstant.Verifier.DELETE_PROOF_REQUEST)
    public ResponseEntity<EmptyResDto> deleteProofRequest(@PathVariable Long id) {
        return ResponseEntity.ok(proofRequestService.deleteProofRequest(id));
    }

}
