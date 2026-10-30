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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.constant.SubTransactionStatus;
import org.omnione.did.base.db.constant.SubTransactionType;
import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.SubTransaction;
import org.omnione.did.base.db.domain.Transaction;
import org.omnione.did.base.db.domain.VpOffer;
import org.omnione.did.base.db.domain.VpProfile;
import org.omnione.did.base.db.domain.ZkpPolicyProfile;
import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.omnione.did.verifier.v1.common.PolicyCacheService;
import org.omnione.did.base.db.repository.VpProfileRepository;
import org.omnione.did.base.db.repository.ZkpPolicyProfileRepository;
import org.omnione.did.base.db.repository.ZkpProofRequestRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.verifier.v1.admin.service.VerifierInfoQueryService;
import org.omnione.did.verifier.v1.agent.dto.ProofRequestResDto;
import org.omnione.did.verifier.v1.agent.dto.RequestProfileReqDto;
import org.omnione.did.verifier.v1.agent.dto.RequestProfileResDto;
import org.omnione.did.verifier.v1.provider.EcdhSessionProvider;
import org.omnione.did.verifier.v1.model.data.ProofRequestProfile;
import org.omnione.did.verifier.v1.model.request.ProofRequestProfileRequest;
import org.omnione.did.verifier.v1.model.data.ReqE2e;
import org.omnione.did.verifier.v1.model.policy.ZkpPolicy;
import org.omnione.did.verifier.v1.protocol.VerifierService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * VP Profile application service.
 *
 * Handles the application business logic for generating Verify Profiles.
 *
 * Design notes:
 * - The SDK returns a core VerifyProfile directly, so no conversion to an application model is needed
 * - Proof signing is delegated to ProofSigningService
 *
 * Responsibilities:
 * - Creating the E2E session
 * - Generating the core VerifyProfile through the SDK (used as is, without conversion)
 * - Delegating proof signing (ProofSigningService)
 * - Saving the VP Profile
 * - Generating and saving the ZKP ProofRequestProfile
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VpProfileApplicationService {

    private final VerifierService verifierService;
    private final EcdhSessionProvider e2eSessionProvider;
    private final ProofSigningService proofSigningService;
    private final TransactionService transactionService;
    private final VpOfferQueryService vpOfferQueryService;
    private final VpProfileRepository vpProfileRepository;
    private final ZkpPolicyProfileRepository zkpPolicyProfileRepository;
    private final ZkpProofRequestRepository zkpProofRequestRepository;
    private final PolicyCacheService policyCacheService;
    private final VerifierInfoQueryService verifierInfoQueryService;
    private final ObjectMapper objectMapper;

    /**
     * Generates a Verify Profile.
     *
     * The SDK returns a core VerifyProfile directly, so it is used without conversion.
     *
     * @param requestProfileReqDto Request DTO
     * @return Profile response DTO
     */
    public RequestProfileResDto requestProfile(RequestProfileReqDto requestProfileReqDto) {
        log.debug("=== Starting requestProfile ===");

        try {
            // 1. Retrieve the transaction
            Transaction transaction = findTransactionByRequestDto(requestProfileReqDto);
            VpOffer vpOffer = vpOfferQueryService.findByTransactionId(transaction.getId());

            // 2. Create the E2E session
            ReqE2e reqE2e = e2eSessionProvider.createSession(transaction.getTxId());

            // 3. Generate the core VerifyProfile through the SDK (no conversion needed)
            String profileId = UUID.randomUUID().toString();
            VerifyProfile appProfile = verifierService.requestVerifyProfile(
                    vpOffer.getVpPolicyId(),
                    profileId,
                    reqE2e
            );

            // 4. Sign the proof (delegated to ProofSigningService)
            proofSigningService.signVerifyProfile(appProfile);

            // 5. Save the VP Profile
            saveVpProfile(appProfile, transaction.getId());

            // 6. Save the sub-transaction
            SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());
            transactionService.saveSubTransaction(SubTransaction.builder()
                    .transactionId(transaction.getId())
                    .step(lastSubTransaction.getStep() + 1)
                    .type(SubTransactionType.REQUEST_PROFILE)
                    .status(SubTransactionStatus.COMPLETED)
                    .build());

            log.debug("*** Finished requestProfile ***");

            return RequestProfileResDto.builder()
                    .profile(appProfile)
                    .txId(transaction.getTxId())
                    .build();

        } catch (OpenDidException e) {
            log.error("OpenDidException during requestProfile: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception during requestProfile: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_PROFILE);
        }
    }

    /**
     * Generates a ZKP ProofRequestProfile.
     *
     * @param requestProfileReqDto Request DTO
     * @return ProofRequest response DTO
     */
    public ProofRequestResDto requestProofRequestProfile(RequestProfileReqDto requestProfileReqDto) {
        log.debug("=== Starting requestProofRequestProfile ===");

        try {
            // 1. Retrieve the transaction
            Transaction transaction = findTransactionByRequestDto(requestProfileReqDto);
            VpOffer vpOffer = vpOfferQueryService.findByTransactionId(transaction.getId());

            // 2. Retrieve the ZKP policy
            ZkpPolicy zkpPolicy = loadZkpPolicy(vpOffer.getVpPolicyId());

            // 3. Generate the ProofRequestProfile through the SDK
            String profileId = UUID.randomUUID().toString();
            ReqE2e reqE2e = ReqE2e.builder()
                .curve(zkpPolicy.getCurve())
                .cipher(zkpPolicy.getCipher())
                .padding(zkpPolicy.getPadding())
                .build();

            ProofRequestProfileRequest sdkRequest = ProofRequestProfileRequest.builder()
                .txId(transaction.getTxId())
                .policyId(vpOffer.getVpPolicyId())
                .profileId(profileId)
                .reqE2e(reqE2e)
                .verifierDid(verifierInfoQueryService.getVerifierInfo().getDid())
                .build();

            ProofRequestProfile sdkProfile = verifierService.requestZkpProofRequestProfile(sdkRequest, zkpPolicy);

            // 4. Sign the proof (delegated to ProofSigningService)
            proofSigningService.signZkpProfile(sdkProfile);

            // 5. Convert the SDK profile to the application DTO
            org.omnione.did.base.datamodel.data.ProofRequestProfile appProfile =
                    convertToAppProofRequestProfile(sdkProfile);

            // 6. Save the VP Profile (for ZKP)
            saveZkpVpProfile(appProfile, transaction.getId());

            // 7. Save the sub-transaction
            SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());
            transactionService.saveSubTransaction(SubTransaction.builder()
                    .transactionId(transaction.getId())
                    .step(lastSubTransaction.getStep() + 1)
                    .type(SubTransactionType.REQUEST_PROFILE)
                    .status(SubTransactionStatus.COMPLETED)
                    .build());

            log.debug("*** Finished requestProofRequestProfile ***");

            return ProofRequestResDto.builder()
                    .proofRequestProfile(appProfile)
                    .txId(transaction.getTxId())
                    .build();

        } catch (OpenDidException e) {
            log.error("OpenDidException during requestProofRequestProfile: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception during requestProofRequestProfile: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_PROOF_REQUEST_PROFILE);
        }
    }

    Transaction findTransactionByRequestDto(RequestProfileReqDto requestProfileReqDto) {
        Transaction transaction;
        if (requestProfileReqDto.getTxId() == null || requestProfileReqDto.getTxId().isEmpty()) {
            transaction = transactionService.findTransactionByOfferId(requestProfileReqDto.getOfferId());
        } else {
            transaction = transactionService.findTransactionByTxId(requestProfileReqDto.getTxId());
        }

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new OpenDidException(ErrorCode.TRANSACTION_INVALID);
        }

        if (Instant.now().isAfter(transaction.getExpired_at())) {
            throw new OpenDidException(ErrorCode.TRANSACTION_EXPIRED);
        }

        return transaction;
    }

    void saveVpProfile(VerifyProfile verifyProfile, Long txId) {
        VpProfile vpProfile = new VpProfile();
        vpProfile.setProfileId(verifyProfile.getId());
        vpProfile.setTransactionId(txId);
        try {
            String verifyProfileToJson = objectMapper.writeValueAsString(verifyProfile);
            vpProfile.setVpProfile(verifyProfileToJson);
            vpProfileRepository.save(vpProfile);
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.VERIFY_PROFILE_PARSE_ERROR);
        }
    }

    private void saveZkpVpProfile(org.omnione.did.base.datamodel.data.ProofRequestProfile profile, Long txId) {
        VpProfile vpProfile = new VpProfile();
        vpProfile.setProfileId(profile.getId());
        vpProfile.setTransactionId(txId);
        try {
            String profileJson = objectMapper.writeValueAsString(profile);
            vpProfile.setVpProfile(profileJson);
            vpProfileRepository.save(vpProfile);
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.VERIFY_PROFILE_PARSE_ERROR);
        }
    }

    private ZkpPolicy loadZkpPolicy(String vpPolicyId) {
        try {
            Policy policy = policyCacheService.findByPolicyId(vpPolicyId);

            String zkpProfileId = policy.getPolicyProfileId();
            ZkpPolicyProfile zkpPolicyProfile = zkpPolicyProfileRepository.findByProfileId(zkpProfileId)
                    .orElseThrow(() -> new OpenDidException(ErrorCode.ZKP_POLICY_PROFILE_NOT_FOUND));

            ZkpProofRequest zkpProofRequest = zkpProofRequestRepository
                    .findById(zkpPolicyProfile.getZkpProofRequestId())
                    .orElseThrow(() -> new OpenDidException(ErrorCode.ZKP_PROOF_REQUEST_NOT_FOUND));

            return ZkpPolicy.builder()
                    .policyId(vpPolicyId)
                    .title(zkpPolicyProfile.getTitle())
                    .description(zkpPolicyProfile.getDescription())
                    .language(zkpPolicyProfile.getLanguage())
                    .name(zkpProofRequest.getName())
                    .version(zkpProofRequest.getVersion())
                    .requestedAttributes(zkpProofRequest.getRequestedAttributes())
                    .requestedPredicates(zkpProofRequest.getRequestedPredicates())
                    .curve(zkpProofRequest.getCurve().toString())
                    .cipher(zkpProofRequest.getCipher().toString())
                    .padding(zkpProofRequest.getPadding().toString())
                    .build();

        } catch (OpenDidException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to load ZKP Policy: {}", vpPolicyId, e);
            throw new OpenDidException(ErrorCode.VP_POLICY_NOT_FOUND);
        }
    }

    private org.omnione.did.base.datamodel.data.ProofRequestProfile convertToAppProofRequestProfile(
            ProofRequestProfile sdkProfile) {
        try {
            String sdkProfileJson = new Gson().toJson(sdkProfile);
            // Read back with Gson too: the ZKP SDK models only carry Gson names (PredicateInfo pType/pValue),
            // which Jackson maps to ptype/pvalue and silently drops
            return new Gson().fromJson(sdkProfileJson,
                    org.omnione.did.base.datamodel.data.ProofRequestProfile.class);
        } catch (Exception e) {
            log.error("Failed to convert SDK ProofRequestProfile to Application DTO", e);
            throw new OpenDidException(ErrorCode.VERIFY_PROFILE_PARSE_ERROR);
        }
    }
}
