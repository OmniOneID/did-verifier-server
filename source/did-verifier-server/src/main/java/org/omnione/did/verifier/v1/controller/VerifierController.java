/*
 * Copyright 2024 OmniOne.
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

package org.omnione.did.verifier.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.omnione.did.base.constants.UrlConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.verifier.v1.dto.*;
import org.omnione.did.verifier.v1.service.VerifierService;
import org.springframework.web.bind.annotation.*;


/**
 * The VerifierController class provides methods for requesting and processing verifiable presentations (VPs).
 * It is designed to facilitate the retrieval and validation of VPs from the verifier, ensuring that the data is accurate and up-to-date.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = UrlConstant.Verifier.V1)
public class VerifierController {
    private final VerifierService verifierService;

    /**
     * Requests a VP Offer QR code.
     *
     * @param requestOfferReqDto The request data for VP Offer
     * @return RequestOfferResDto The response containing the VP Offer QR code
     */
    @Operation(summary = "Request VP Offer QR", description = "Creates a VP Offer QR code")
    @PostMapping(UrlConstant.Verifier.REQUEST_OFFER_QR)
    public RequestOfferResDto requestVpOfferbyQR(@RequestBody @Valid RequestOfferReqDto requestOfferReqDto) {
        return verifierService.requestVpOfferbyQR(requestOfferReqDto);
    }

    /**
     * Requests a VP Profile.
     *
     * @param requestProfileReqDto The request data for VP Profile
     * @return RequestProfileResDto The response containing the requested VP Profile
     */
    @PostMapping(value = UrlConstant.Verifier.REQUEST_PROFILE)
    @Operation(summary = "Request VP Profile", description = "Request the profile from the VP")
    public RequestProfileResDto requestProfile(@RequestBody @Valid RequestProfileReqDto requestProfileReqDto) {
        return verifierService.requestProfile(requestProfileReqDto);
    }

    /**
     * Requests verification of a VP.
     *
     * @param requestVerifyReqDto The request data for verification
     * @return RequestVerifyResDto The response containing the verification result
     */
    @PostMapping(value = UrlConstant.Verifier.REQUEST_VERIFY)
    @Operation(summary = "Request Verify", description = "Requesting verification from the VP")
    public RequestVerifyResDto requestVerify(@RequestBody @Valid RequestVerifyReqDto requestVerifyReqDto) {
        return verifierService.requestVerify(requestVerifyReqDto);
    }

    /**
     * Confirms the verification of a VP.
     *
     * @param confirmVerifyReqDto The request data for confirming verification
     * @return ConfirmVerifyResDto The response containing the confirmation result
     */
    @PostMapping(value = UrlConstant.Verifier.CONFIRM_VERIFY)
    @Operation(summary = "Request Verify confirm", description = "Request confirmation of verification from the VP")
    public ConfirmVerifyResDto confirmVerify(@RequestBody @Valid ConfirmVerifyReqDto confirmVerifyReqDto) {
        return verifierService.confirmVerify(confirmVerifyReqDto);
    }

}

