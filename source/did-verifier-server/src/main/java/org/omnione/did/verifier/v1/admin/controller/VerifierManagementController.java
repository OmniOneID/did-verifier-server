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
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.admin.dto.*;
import org.omnione.did.verifier.v1.admin.service.VerifierManagementService;
import org.omnione.did.verifier.v1.common.dto.EmptyResDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.ADMIN)
public class VerifierManagementController {
    private final VerifierManagementService verifierManagementService;

    @Operation(summary = "Get Verifier Info", description = "get Verifier Info")
    @GetMapping(UrlConstant.Verifier.GET_VERIFIER_INFO)
    public GetVerifierInfoReqDto getVerifierInfo() {
        return verifierManagementService.getVerifierInfo();
    }

    @RequestMapping(value = "/certificate-vc", method = RequestMethod.POST)
    public EmptyResDto createCertificateVc(@RequestBody SendCertificateVcReqDto sendCertificateVcReqDto) {
        return verifierManagementService.createCertificateVc(sendCertificateVcReqDto);
    }

    @RequestMapping(value = "/entity-info", method = RequestMethod.POST)
    public EmptyResDto updateEntityInfo(@RequestBody SendEntityInfoReqDto sendEntityInfoReqDto) {
        return verifierManagementService.updateEntityInfo(sendEntityInfoReqDto);
    }

    @RequestMapping(value = "/register-verifier-info", method = RequestMethod.POST)
    public VerifierInfoResDto registerVerifierInfo(@RequestBody RegisterVerifierInfoReqDto registerVerifierInfoReqDto) {
        return verifierManagementService.registerVerifierInfo(registerVerifierInfoReqDto);
    }

    @RequestMapping(value = "/generate-did-auto", method = RequestMethod.POST)
    public Map<String, Object> generateVerifierDidDocumentAuto() {
        return verifierManagementService.registerVerifierDidDocumentAuto();
    }

    @RequestMapping(value = "/register-did", method = RequestMethod.POST)
    public EmptyResDto requestRegisterDid(@RequestBody RequestRegisterDidReqDto requestRegisterDidReqDto) {
        return verifierManagementService.requestRegisterDid(requestRegisterDidReqDto);
    }

    @GetMapping(value = "/request-status")
    public RequestEntityStatusResDto requestEntityStatus() {
        return verifierManagementService.requestEntityStatus();
    }

    @PostMapping(value = "/request-enroll-entity")
    public Map<String, Object> requestEnrollEntity() {
        return verifierManagementService.enrollEntity();
    }
}
