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
import org.omnione.did.verifier.v1.admin.dto.ProofRequestDto;
import org.omnione.did.verifier.v1.admin.dto.ZkpPolicyProfileDto;
import org.omnione.did.verifier.v1.admin.service.ZkpProfileService;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class ZkpProfileController {
    private final ZkpProfileService zkpProfileService;

    @Operation(summary = "Get ZKP Profile List", description = "Get a list of zkp profile")
    @GetMapping(UrlConstant.Verifier.GET_ZKP_PROFILE)
    public Page<ZkpPolicyProfileDto> searchZkpProfileList(String searchKey, String searchValue, Pageable pageable) {
        return zkpProfileService.searchZkpProfileList(searchKey, searchValue, pageable);
    }

    @Operation(summary = "Save ZKP Profile", description = "Save a new zkp profile")
    @PostMapping(UrlConstant.Verifier.SAVE_ZKP_PROFILE)
    public ResponseEntity<EmptyResDto> createZkpProfile(@RequestBody ZkpPolicyProfileDto zkpPolicyProfileDto) {
        return ResponseEntity.ok(zkpProfileService.createZkpProfile(zkpPolicyProfileDto));
    }

    @Operation(summary = "Get ZKP Profile Info", description = "Get zkp profile info")
    @GetMapping(UrlConstant.Verifier.GET_ZKP_PROFILE_INFO)
    public ResponseEntity<ZkpPolicyProfileDto> getProofRequestInfo(@PathVariable Long id) {
        return ResponseEntity.ok(zkpProfileService.getZkpProfileInfo(id));
    }

    @Operation(summary = "Update ZKP Profile", description = "Update a zkp profile")
    @PutMapping(Verifier.UPDATE_ZKP_PROFILE)
    public ResponseEntity<EmptyResDto> updateZkpProfile(@RequestBody ZkpPolicyProfileDto zkpPolicyProfileDto) {
        return ResponseEntity.ok(zkpProfileService.updateZkpProfile(zkpPolicyProfileDto));
    }

    @Operation(summary = "Delete ZKP Profile", description = "Delete a zkp profile")
    @DeleteMapping(Verifier.DELETE_ZKP_PROFILE)
    public ResponseEntity<EmptyResDto> deleteZkpProfile(@PathVariable Long id) {
        return ResponseEntity.ok(zkpProfileService.deleteZkpProfile(id));
    }
}
