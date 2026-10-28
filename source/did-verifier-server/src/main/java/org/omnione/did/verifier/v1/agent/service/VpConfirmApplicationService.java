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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.Transaction;
import org.omnione.did.base.db.domain.VpOffer;
import org.omnione.did.base.db.domain.VpSubmit;
import org.omnione.did.base.db.repository.VpOfferRepository;
import org.omnione.did.base.db.repository.VpSubmitRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.data.model.vc.Claim;
import org.omnione.did.data.model.vc.VerifiableCredential;
import org.omnione.did.data.model.vp.VerifiablePresentation;
import org.omnione.did.verifier.v1.agent.dto.ConfirmVerifyReqDto;
import org.omnione.did.verifier.v1.agent.dto.ConfirmVerifyResDto;
import org.omnione.did.verifier.v1.model.enums.OfferType;
import org.omnione.did.verifier.v1.model.response.VerificationConfirmResult;
import org.omnione.did.verifier.v1.protocol.VerifierService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * VP verification confirmation application service.
 *
 * Handles the application business logic for confirming VP verification results and extracting claims.
 *
 * Responsibilities:
 * - Retrieving the transaction
 * - Retrieving the VpSubmit
 * - Extracting claims through the SDK
 * - Returning the verification result
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VpConfirmApplicationService {

    private final VerifierService verifierService;
    private final TransactionService transactionService;
    private final VpSubmitRepository vpSubmitRepository;
    private final VpOfferRepository vpOfferRepository;

    /**
     * Confirms the verification result and extracts claims.
     *
     * @param confirmVerifyReqDto Request DTO
     * @return Confirmation result DTO (verification success flag and claim list)
     */
    public ConfirmVerifyResDto confirmVerify(ConfirmVerifyReqDto confirmVerifyReqDto) {
        log.debug("=== Starting confirmVerify ===");

        try {
            Transaction transaction = transactionService.findTransactionByOfferId(
                    confirmVerifyReqDto.getOfferId());
            VpSubmit vpSubmit = vpSubmitRepository.findByTransactionId(transaction.getId());

            if (vpSubmit == null) {
                return ConfirmVerifyResDto.builder()
                        .result(false)
                        .build();
            }

            // Look up the VpOffer to determine the OfferType
            VpOffer vpOffer = vpOfferRepository.findByOfferId(confirmVerifyReqDto.getOfferId())
                    .orElseThrow(() -> new OpenDidException(ErrorCode.VP_OFFER_NOT_FOUND));

            OfferType sdkOfferType = OfferType.valueOf(vpOffer.getOfferType());

            // Extract claims through the SDK
            VerificationConfirmResult confirmResult = verifierService.confirmVerification(
                    transaction.getTxId(),
                    vpSubmit.getVp(),
                    true,
                    sdkOfferType
            );

            List<Claim> claims = extractClaimsFromResult(confirmResult, vpSubmit.getVp());

            log.debug("*** Finished confirmVerify ***");

            return ConfirmVerifyResDto.builder()
                    .result(Boolean.TRUE.equals(confirmResult.getVerified()))
                    .claims(claims)
                    .build();

        } catch (OpenDidException e) {
            log.error("OpenDidException during confirmVerify: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception during confirmVerify: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_CONFIRM_VERIFY);
        }
    }

    private List<Claim> extractClaimsFromResult(VerificationConfirmResult result, String vpJson) {
        List<Claim> claims = new ArrayList<>();
        try {
            VerifiablePresentation vp = new VerifiablePresentation();
            vp.fromJson(vpJson);
            List<VerifiableCredential> vcs = vp.getVerifiableCredential();
            if (vcs != null) {
                vcs.forEach(vc -> {
                    if (vc.getCredentialSubject() != null
                            && vc.getCredentialSubject().getClaims() != null) {
                        claims.addAll(vc.getCredentialSubject().getClaims());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Failed to extract claims from VP", e);
        }
        return claims;
    }
}
