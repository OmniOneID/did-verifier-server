/*
 * Copyright 2026 OmniOne.
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

package org.omnione.did.verifier.v1.protocol.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateRequest;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateResponse;
import org.omnione.did.verifier.v1.protocol.api.dto.StatusResponse;
import org.omnione.did.verifier.v1.protocol.orchestrator.StatusQueryService;
import org.omnione.did.verifier.v1.protocol.orchestrator.VerificationOrchestrator;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.V2)
@Tag(name = "Verification", description = "Protocol-agnostic verification API")
public class VerificationController {

    private final VerificationOrchestrator verificationOrchestrator;
    private final StatusQueryService statusQueryService;

    @Operation(summary = "Initiate Verification",
            description = "Initiates verification based on policy's protocol type (DID VP or OID4VP)")
    @PostMapping(UrlConstant.Verifier.INITIATE)
    public InitiateResponse initiate(@RequestBody @Valid InitiateRequest request) {
        return verificationOrchestrator.initiate(request);
    }

    @Operation(summary = "Get Verification Status",
            description = "Returns the current status of a verification session")
    @GetMapping(UrlConstant.Verifier.STATUS)
    public StatusResponse getStatus(@PathVariable String sessionId) {
        return statusQueryService.getStatus(sessionId);
    }
}
