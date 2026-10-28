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

package org.omnione.did.verifier.v1.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.verifier.v1.agent.dto.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Facade implementation of ApplicationVerifierService.
 *
 * Delegates directly to four feature-specific services.
 * Business logic is handled in each service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!sample")
public class ApplicationVerifierServiceImpl implements ApplicationVerifierService {

    private final VpOfferApplicationService offerService;
    private final VpProfileApplicationService profileService;
    private final VpVerificationApplicationService verificationService;
    private final VpConfirmApplicationService confirmService;

    @Override
    public RequestOfferResDto requestVpOfferbyQR(RequestOfferReqDto requestOfferReqDto) {
        return offerService.requestVpOfferbyQR(requestOfferReqDto);
    }

    @Override
    public RequestProfileResDto requestProfile(RequestProfileReqDto requestProfileReqDto) {
        return profileService.requestProfile(requestProfileReqDto);
    }

    @Override
    public RequestVerifyResDto requestVerify(RequestVerifyReqDto requestVerifyReqDto) {
        return verificationService.requestVerify(requestVerifyReqDto);
    }

    @Override
    public ConfirmVerifyResDto confirmVerify(ConfirmVerifyReqDto confirmVerifyReqDto) {
        return confirmService.confirmVerify(confirmVerifyReqDto);
    }

    @Override
    public ProofRequestResDto requestProofRequestProfile(RequestProfileReqDto requestProfileReqDto) {
        return profileService.requestProofRequestProfile(requestProfileReqDto);
    }

    @Override
    public RequestVerifyResDto requestVerifyProof(RequestVerifyProofReqDto requestVerifyProofReqDto) {
        return verificationService.requestVerifyProof(requestVerifyProofReqDto);
    }
}
