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

import org.omnione.did.base.annotation.ArticleLog;
import org.omnione.did.base.constants.ActionType;
import org.omnione.did.base.constants.ApiType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.oid4vc.oid4vp.dto.ServiceResult;
import org.omnione.did.verifier.v1.protocol.api.dto.Oid4vpResponseRequest;
import org.omnione.did.verifier.v1.protocol.api.dto.Oid4vpResponseResult;
import org.omnione.did.verifier.v1.protocol.service.OID4VPService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Oid4vp.BASE)
@Tag(name = "OID4VP", description = "OID4VP protocol endpoints")
public class OID4VPController {

    private final OID4VPService oid4vpService;

    @Operation(summary = "Get Authorization Request",
            description = "Wallet calls this endpoint to retrieve the Authorization Request JWT via request_uri")
    @GetMapping(UrlConstant.Oid4vp.REQUEST)
    public ResponseEntity<String> getAuthorizationRequest(@PathVariable String requestId) {
        ServiceResult<String> result = oid4vpService.getAuthorizationRequest(requestId);

        String contentType = result.getContentType() != null
                ? result.getContentType()
                : MediaType.APPLICATION_JSON_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(result.getData());
    }

    @Operation(summary = "Receive VP Token Response",
            description = "Wallet submits VP Token via direct_post(.jwt) response mode")
    @ArticleLog(name = "[OID4VP] Receive Response", apiType = ApiType.AGENT, targetType = "O_I_D4_V_P", actionType = ActionType.NONE)
    @PostMapping(UrlConstant.Oid4vp.RESPONSE)
    public ResponseEntity<Oid4vpResponseResult> receiveResponse(
            @RequestParam(value = "vp_token", required = false) String vpToken,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "presentation_submission", required = false) String presentationSubmission,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @RequestParam(value = "response", required = false) String response) {
        // direct_post responses are sent as application/x-www-form-urlencoded.
        // Form binding ignores @JsonProperty (snake_case), so the fields are mapped explicitly with @RequestParam
        // and then assembled into the DTO.
        Oid4vpResponseRequest request = new Oid4vpResponseRequest(
                vpToken, state, presentationSubmission, error, errorDescription, response);
        Oid4vpResponseResult responseResult = oid4vpService.receiveResponse(request);
        // Verification failures (invalid VP, vp_token parse failure) are client errors → 400.
        // Internal server errors (unexpected exceptions) are handled as 500 by GlobalControllerAdvice/Spring.
        if ("FAILED".equals(responseResult.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseResult);
        }
        return ResponseEntity.ok(responseResult);
    }
}
