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

package org.omnione.did.verifier.v1.service;
import org.omnione.did.verifier.v1.dto.*;

/**
 * Verifier service interface for handling Verifiable Presentations (VPs) and user verification
 */
public interface VerifierService {

    /**
     * Requests a VP offer via QR code.
     *
     * @param requestOfferReqDto Request data for the VP offer.
     * @return VP offer response.
     */
    RequestOfferResDto requestVpOfferbyQR(RequestOfferReqDto requestOfferReqDto);

    /**
     * Requests user profile information.
     *
     * @param requestProfileReqDto Request data for the profile.
     * @return Profile response.
     */
    RequestProfileResDto requestProfile(RequestProfileReqDto requestProfileReqDto);

    /**
     * Requests claim verification.
     *
     * @param requestVerifyReqDto Request data for the verification.
     * @return Verification response.
     */
    RequestVerifyResDto requestVerify(RequestVerifyReqDto requestVerifyReqDto);

    /**
     * Confirms a verification result.
     *
     * @param confirmVerifyReqDto Request data for confirmation.
     * @return Confirmation response.
     */
    ConfirmVerifyResDto confirmVerify(ConfirmVerifyReqDto confirmVerifyReqDto);
}
