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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.datamodel.data.AccE2e;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.db.constant.SubTransactionStatus;
import org.omnione.did.base.db.constant.SubTransactionType;
import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.domain.SubTransaction;
import org.omnione.did.base.db.domain.Transaction;
import org.omnione.did.base.db.domain.VpProfile;
import org.omnione.did.base.db.domain.VpSubmit;
import org.omnione.did.base.db.repository.VpProfileRepository;
import org.omnione.did.base.db.repository.VpSubmitRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.common.exception.CommonSdkException;
import org.omnione.did.common.util.DidUtil;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.common.util.crypto.BaseCryptoUtil;
import org.omnione.did.common.util.digest.BaseDigestUtil;
import org.omnione.did.core.manager.DidManager;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.Proof;
import org.omnione.did.data.model.did.VerificationMethod;
import org.omnione.did.data.model.profile.Filter;
import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.data.model.vp.VerifiablePresentation;
import org.omnione.did.verifier.v1.agent.dto.RequestVerifyProofReqDto;
import org.omnione.did.verifier.v1.agent.dto.RequestVerifyReqDto;
import org.omnione.did.verifier.v1.agent.dto.RequestVerifyResDto;
import org.omnione.did.verifier.v1.common.service.StorageService;
import org.omnione.did.verifier.v1.common.service.VpSubmitAuditService;
import org.omnione.did.verifier.v1.model.data.ProofVerifyParam;
import org.omnione.did.verifier.v1.model.request.VpVerificationRequest;
import org.omnione.did.verifier.v1.model.request.ZkpVerificationRequest;
import org.omnione.did.verifier.v1.model.response.ZkpVerificationResult;
import org.omnione.did.verifier.v1.protocol.VerifierService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * VP verification application service.
 *
 * Handles the application business logic for VP/ZKP verification.
 *
 * Design notes:
 * - The core Filter is passed directly to VpVerificationRequest without conversion
 *
 * Responsibilities:
 * - Validating the transaction status
 * - Verifying the AccE2e proof
 * - Verifying VPs/ZKP proofs through the SDK
 * - Saving the VpSubmit and updating the transaction status
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VpVerificationApplicationService {

    /** Credential format of VPs submitted over the native OpenDID (DID_VP) protocol. Shown in VP History. */
    private static final String FORMAT_OPENDID_VC = "opendid_vc";

    private final VerifierService verifierService;
    private final TransactionService transactionService;
    private final VpProfileRepository vpProfileRepository;
    private final VpSubmitRepository vpSubmitRepository;
    private final VpSubmitAuditService vpSubmitAuditService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    /**
     * Verifies a VP.
     *
     * Passes the core Filter directly to the SDK without conversion.
     *
     * @param requestVerifyReqDto Request DTO
     * @return Verification response DTO
     */
    public RequestVerifyResDto requestVerify(RequestVerifyReqDto requestVerifyReqDto) {
        log.debug("=== Starting requestVerify ===");

        try {
            // 1. Validate the transaction
            Transaction transaction = transactionService.findTransactionByTxId(requestVerifyReqDto.getTxId());
            SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());
            validateTransaction(transaction, lastSubTransaction);

            // 2. Retrieve the profile
            VerifyProfile findProfile = findProfile(requestVerifyReqDto.getTxId());

            // 3. Verify the AccE2e proof
            if (Objects.nonNull(requestVerifyReqDto.getAccE2e().getProof())) {
                verifyAccE2eProof(requestVerifyReqDto.getAccE2e());
            }

            // 4. Build the VP verification request (using the core Filter directly)
            String serverNonce = findProfile.getProfile().getProcess().getVerifierNonce();
            Filter coreFilter = findProfile.getProfile().getFilter();

            VpVerificationRequest verifyRequest = VpVerificationRequest.builder()
                    .txId(requestVerifyReqDto.getTxId())
                    .encHolderPublicKey(requestVerifyReqDto.getAccE2e().getPublicKey())
                    .encVp(requestVerifyReqDto.getEncVp())
                    .iv(requestVerifyReqDto.getAccE2e().getIv())
                    .verifierNonce(serverNonce)
                    .requiredAuthType(findProfile.getProfile().getProcess().getAuthType())
                    .filter(coreFilter)
                    .build();

            // 5. Verify the VP through the SDK
            String vpJson = verifierService.verifyPresentation(verifyRequest);

            // 6. Parse the VP and save the result
            VerifiablePresentation vp = new VerifiablePresentation();
            vp.fromJson(vpJson);

            vpSubmitRepository.save(VpSubmit.builder()
                    .transactionId(transaction.getId())
                    .vp(vpJson)
                    .holderDid(vp.getHolder())
                    .format(FORMAT_OPENDID_VC)
                    .build());

            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.COMPLETED);

            transactionService.saveSubTransaction(SubTransaction.builder()
                    .transactionId(transaction.getId())
                    .step(lastSubTransaction.getStep() + 1)
                    .type(SubTransactionType.REQUEST_VERIFY)
                    .status(SubTransactionStatus.COMPLETED)
                    .build());

            log.debug("*** Finished requestVerify ***");

            return RequestVerifyResDto.builder()
                    .txId(requestVerifyReqDto.getTxId())
                    .build();

        } catch (OpenDidException e) {
            log.error("OpenDidException during requestVerify: {}", e.getErrorCode().getMessage());
            handleTxFailure(requestVerifyReqDto.getTxId(), e.getErrorCode().getCode(), FORMAT_OPENDID_VC);
            throw e;
        } catch (Exception e) {
            log.error("Exception during requestVerify: {}", e.getMessage(), e);
            handleTxFailure(requestVerifyReqDto.getTxId(), ErrorCode.FAILED_TO_REQUEST_VERIFY.getCode(), FORMAT_OPENDID_VC);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_VERIFY);
        }
    }

    /**
     * Verifies a ZKP proof.
     *
     * @param requestVerifyProofReqDto Request DTO
     * @return Verification response DTO
     */
    public RequestVerifyResDto requestVerifyProof(RequestVerifyProofReqDto requestVerifyProofReqDto) {
        log.debug("=== Starting requestVerifyProof ===");

        try {
            // 1. Validate the transaction
            Transaction transaction = transactionService.findTransactionByTxId(requestVerifyProofReqDto.getTxId());
            SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());
            validateTransaction(transaction, lastSubTransaction);

            // 2. Retrieve the ProofRequestProfile
            org.omnione.did.base.datamodel.data.ProofRequestProfile findProfile =
                findProofRequestProfile(requestVerifyProofReqDto.getTxId());

            // 3. Verify the AccE2e proof
            if (Objects.nonNull(requestVerifyProofReqDto.getAccE2e().getProof())) {
                verifyAccE2eProof(requestVerifyProofReqDto.getAccE2e());
            }

            // 4. Build the SDK verification request
            // TODO: Populate ZKP credential-specific verify params (issuer key, revocation status, etc.) when needed
            List<ProofVerifyParam> proofVerifyParams = new ArrayList<>();

            ZkpVerificationRequest.AccE2e sdkAccE2e = ZkpVerificationRequest.AccE2e.builder()
                .publicKey(requestVerifyProofReqDto.getAccE2e().getPublicKey())
                .iv(requestVerifyProofReqDto.getAccE2e().getIv())
                .proof(requestVerifyProofReqDto.getAccE2e().getProof())
                .build();

            ZkpVerificationRequest sdkRequest = ZkpVerificationRequest.builder()
                .txId(requestVerifyProofReqDto.getTxId())
                .encProof(requestVerifyProofReqDto.getEncProof())
                .iv(requestVerifyProofReqDto.getAccE2e().getIv())
                .accE2e(sdkAccE2e)
                .nonce(requestVerifyProofReqDto.getNonce())
                .proofRequest(findProfile.getProfile().getProofRequest())
                .proofVerifyParams(proofVerifyParams)
                .build();

            // 5. Verify the ZKP proof through the SDK
            ZkpVerificationResult result = verifierService.verifyZkpProof(sdkRequest);

            if (!result.isVerified()) {
                throw new OpenDidException(ErrorCode.FAILED_TO_VERIFY_PROOF);
            }

            // 6. Save the result
            vpSubmitRepository.save(VpSubmit.builder()
                    .transactionId(transaction.getId())
                    .vp("Zkp Proof")
                    .holderDid("Zkp VP Holder")
                    .build());

            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.COMPLETED);

            transactionService.saveSubTransaction(SubTransaction.builder()
                    .transactionId(transaction.getId())
                    .step(lastSubTransaction.getStep() + 1)
                    .type(SubTransactionType.REQUEST_VERIFY)
                    .status(SubTransactionStatus.COMPLETED)
                    .build());

            log.debug("*** Finished requestVerifyProof ***");

            return RequestVerifyResDto.builder()
                    .txId(requestVerifyProofReqDto.getTxId())
                    .build();

        } catch (OpenDidException e) {
            log.error("OpenDidException during requestVerifyProof: {}", e.getErrorCode().getMessage());
            handleTxFailure(requestVerifyProofReqDto.getTxId(), e.getErrorCode().getCode(), null);
            throw e;
        } catch (Exception e) {
            log.error("Exception during requestVerifyProof: {}", e.getMessage(), e);
            handleTxFailure(requestVerifyProofReqDto.getTxId(), ErrorCode.FAILED_TO_VERIFY_PROOF.getCode(), null);
            throw new OpenDidException(ErrorCode.FAILED_TO_VERIFY_PROOF);
        }
    }

    VerifyProfile findProfile(String txId) {
        try {
            Transaction transaction = transactionService.findTransactionByTxId(txId);
            if (transaction == null) {
                throw new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND);
            }
            VpProfile vpProfile = vpProfileRepository
                    .findTop1ByTransactionIdOrderByCreatedAtDesc(transaction.getId())
                    .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));
            return objectMapper.readValue(vpProfile.getVpProfile(), VerifyProfile.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse VP profile for txId: {}", txId, e);
            throw new OpenDidException(ErrorCode.VP_PROFILE_PARSE_ERROR);
        }
    }

    private org.omnione.did.base.datamodel.data.ProofRequestProfile findProofRequestProfile(String txId) {
        try {
            Transaction transaction = transactionService.findTransactionByTxId(txId);
            if (transaction == null) {
                throw new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND);
            }
            VpProfile vpProfile = vpProfileRepository.findByTransactionId(transaction.getId())
                    .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));
            return objectMapper.readValue(vpProfile.getVpProfile(),
                    org.omnione.did.base.datamodel.data.ProofRequestProfile.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ProofRequestProfile for txId: {}", txId, e);
            throw new OpenDidException(ErrorCode.VP_PROFILE_PARSE_ERROR);
        }
    }

    private void validateTransaction(Transaction transaction, SubTransaction subTransaction) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new OpenDidException(ErrorCode.TRANSACTION_INVALID);
        }
        if (java.time.Instant.now().isAfter(transaction.getExpired_at())) {
            throw new OpenDidException(ErrorCode.TRANSACTION_EXPIRED);
        }

        Set<SubTransactionType> VALID_TYPES = EnumSet.of(
                SubTransactionType.REQUEST_PROFILE,
                SubTransactionType.REQUEST_OFFER,
                SubTransactionType.REQUEST_VERIFY
        );
        if (!VALID_TYPES.contains(subTransaction.getType())) {
            throw new OpenDidException(ErrorCode.SUB_TRANSACTION_INVALID);
        }
    }

    private void verifyAccE2eProof(AccE2e accE2e) {
        try {
            Proof proof = accE2e.getProof();
            String verificationMethod = proof.getVerificationMethod();
            DidDocument holderDidDoc = storageService.findDidDoc(verificationMethod);
            DidManager didManager = new DidManager();
            didManager.parse(holderDidDoc.toJson());
            String keyId = DidUtil.extractKeyId(verificationMethod);
            VerificationMethod publicKeyByKeyId = didManager.getVerificationMethodByKeyId(keyId);

            Proof tmpProof = new Proof();
            tmpProof.setType(proof.getType());
            tmpProof.setCreated(proof.getCreated());
            tmpProof.setProofPurpose(proof.getProofPurpose());
            tmpProof.setVerificationMethod(proof.getVerificationMethod());
            accE2e.setProof(tmpProof);

            String accE2eString = JsonUtil.serializeAndSort(accE2e);
            try {
                BaseCryptoUtil.verifySignature(publicKeyByKeyId.getPublicKeyMultibase(), proof.getProofValue(),
                        BaseDigestUtil.generateHash(accE2eString.getBytes(StandardCharsets.UTF_8)),
                        EccCurveType.SECP_256_R1.toCommonSdkEccCurveType());
            } catch (CommonSdkException e) {
                // Hash/signature failures were reported as ACC_E2E_ERROR, not JSON_PARSE_ERROR
                throw new OpenDidException(ErrorCode.ACC_E2E_ERROR);
            }

        } catch (CommonSdkException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        } catch (Exception e) {
            throw new OpenDidException(ErrorCode.ACC_E2E_ERROR);
        }
    }

    void handleTxFailure(String txId, String errorCode, String format) {
        Long transactionId = null;
        try {
            Transaction transaction = transactionService.findTransactionByTxId(txId);
            if (transaction != null) {
                transactionId = transaction.getId();
            }
        } catch (Exception e) {
            log.warn("handleTxFailure: failed to resolve transaction by txId {}", txId, e);
        }

        if (transactionId != null) {
            vpSubmitAuditService.recordFailure(transactionId, null, null, errorCode, format);
        }

        try {
            transactionService.updateErrorTransactionStatus(txId, TransactionStatus.FAILED);
        } catch (Exception ex) {
            log.warn("Failed to update transaction status: {}", txId, ex);
        }
    }
}
