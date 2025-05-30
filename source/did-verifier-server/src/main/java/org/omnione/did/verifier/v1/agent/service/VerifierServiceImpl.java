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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.omnione.did.ContractFactory;
import org.omnione.did.base.datamodel.data.*;
import org.omnione.did.base.datamodel.enums.*;
import org.omnione.did.base.db.constant.SubTransactionStatus;
import org.omnione.did.base.db.constant.SubTransactionType;
import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.constant.TransactionType;
import org.omnione.did.base.db.domain.*;
import org.omnione.did.base.db.repository.*;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseCoreDidUtil;
import org.omnione.did.base.util.BaseCryptoUtil;
import org.omnione.did.base.util.BaseDigestUtil;
import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.common.exception.CommonSdkException;
import org.omnione.did.common.util.DateTimeUtil;
import org.omnione.did.common.util.DidUtil;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.core.data.rest.VpVerifyParam;
import org.omnione.did.core.exception.CoreException;
import org.omnione.did.core.manager.DidManager;
import org.omnione.did.core.manager.VpManager;
import org.omnione.did.crypto.enums.MultiBaseType;
import org.omnione.did.crypto.exception.CryptoException;
import org.omnione.did.crypto.keypair.EcKeyPair;
import org.omnione.did.crypto.keypair.KeyPairInterface;
import org.omnione.did.crypto.util.MultiBaseUtils;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.Proof;
import org.omnione.did.data.model.did.VerificationMethod;
import org.omnione.did.data.model.enums.did.ProofType;
import org.omnione.did.data.model.profile.Filter;
import org.omnione.did.data.model.profile.Process;
import org.omnione.did.data.model.profile.ReqE2e;
import org.omnione.did.data.model.profile.verify.InnerVerifyProfile;
import org.omnione.did.data.model.profile.verify.VerifyProcess;
import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.data.model.provider.ProviderDetail;
import org.omnione.did.data.model.util.json.GsonWrapper;
import org.omnione.did.data.model.vc.Claim;
import org.omnione.did.data.model.vc.CredentialSchema;
import org.omnione.did.data.model.vc.VerifiableCredential;
import org.omnione.did.data.model.vp.VerifiablePresentation;
import org.omnione.did.verifier.v1.admin.dto.ProcessDTO;
import org.omnione.did.verifier.v1.admin.service.VerifierInfoQueryService;
import org.omnione.did.verifier.v1.agent.dto.*;

import org.omnione.did.verifier.v1.agent.service.sample.ZkpTestConstants;
import org.omnione.did.verifier.v1.common.service.StorageService;

import org.omnione.did.zkp.core.manager.ZkpCredentialManager;
import org.omnione.did.zkp.core.manager.ZkpProofManager;
import org.omnione.did.zkp.crypto.constant.ZkpCryptoConstants;
import org.omnione.did.zkp.crypto.util.BigIntegerUtil;
import org.omnione.did.zkp.datamodel.definition.CredentialDefinition;
import org.omnione.did.zkp.datamodel.proof.Identifiers;
import org.omnione.did.zkp.datamodel.proof.verifyparam.ProofVerifyParam;
import org.omnione.did.zkp.datamodel.proofrequest.ProofRequest;
import org.omnione.did.zkp.exception.ZkpException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.time.Instant;
import java.util.*;

import static org.omnione.did.base.util.BaseCryptoUtil.*;
import static org.omnione.did.common.util.JsonUtil.serializeAndSort;

/**
 * VerifierServiceImpl class
 * This class implements the core functionalities of the Verifier service in a Decentralized Identity (DID) system.
 * It handles various operations related to Verifiable Presentations (VPs) and the verification process.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Profile("!sample")
public class VerifierServiceImpl implements VerifierService {
    private final TransactionService transactionService;
    private final E2EQueryService e2EQueryService;
    private final VpOfferQueryService vpOfferQueryService;
    private final VpProfileRepository vpProfileRepository;
    private final VpSubmitRepository vpSubmitRepository;
    private final FileWalletService walletService;
    private final StorageService storageService;
    private final DidDocService didDocService;
    private final VerifierInfoQueryService verifierInfoQueryService;
    private final PolicyRepository policyRepository;
    private final PayloadRepository payloadRepository;
    private final PolicyProfileRepository policyProfileRepository;
    private final VpFilterRepository vpFilterRepository;
    private final VpProcessRepository vpProcessRepository;
    private final ObjectMapper objectMapper;
    private final ZkpPolicyProfileRepository zkpPolicyProfileRepository;
    private final ZkpProofRequestRepository zkpProofRequestRepository;
    private final VpOfferRepository vpOfferRepository;


    /**
     * Requests a VP Offer via QR code.
     * This method initiates the verification process by creating a VP Offer that can be presented as a QR code.
     *
     * @param requestOfferReqDto The request data for VP Offer, containing details like mode, service, and device
     * @return RequestOfferResDto The response data for VP Offer, including a transaction ID and payload
     * @throws OpenDidException If there's an error in the DID operations or offer creation process
     */
    @Override
    public RequestOfferResDto requestVpOfferbyQR(RequestOfferReqDto requestOfferReqDto) {

        log.debug("=== Starting Requesting VpOfferbyQR ===");
        try{
            log.debug("\t validate requestOfferReqDto and get VpPayload and VpPolicyId");
            VerifyOfferResult verifyOfferResult = getPolicyAndValidate(requestOfferReqDto);
            VerifyOfferPayload payload = verifyOfferResult.getOfferPayload();
            log.debug("\t transaction and sub-transaction creation");
            Transaction transaction = createAndSaveTransaction();
            createAndSaveSubTransaction(transaction.getId());

            String vpOfferId = UUID.randomUUID().toString();
            payload.setOfferId(vpOfferId);

            log.debug("\t Saving VP Offer");
            SaveVpOffer(transaction.getId(), vpOfferId,
                    verifyOfferResult,
                    JsonUtil.serializeToJson(payload), Instant.parse(payload.getValidUntil()));

            log.debug("*** Finished request VpOfferbyQR ***");

            return RequestOfferResDto.builder()
                    .txId(transaction.getTxId())
                    .payload(payload)
                    .build();

        } catch (OpenDidException e){
            log.error("OpenDidException occurred during Requesting VP Offer: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred during Requesting VP Offer: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_OFFER_QR);
        }
    }

    /**
     * Requests a VP Profile.
     * This method creates, signs, and saves a VP Profile based on the request data.
     * It's a crucial step in preparing for the verification process.
     *
     * @param requestProfileReqDto The request data for VP Profile, including offer ID or transaction ID
     * @return RequestProfileResDto The response data containing the created VP Profile and transaction ID
     * @throws OpenDidException If there's an error in the DID operations or profile creation process
     */
    @Override
    public RequestProfileResDto requestProfile(RequestProfileReqDto requestProfileReqDto) {
        try {
            log.info("=== Starting requestProfile ===");
            log.debug("\t --> Retrieving transaction information");

            Transaction transaction = findTransactionByRequestDto(requestProfileReqDto);
            VpOffer vpOffer = findVpOfferByTransaction(transaction);

            log.debug("\t --> Retrieving VerifyProfile by policyId in VP Offer");
            VerifyProfile verifyProfile = getVerifyProfileFromPolicy(vpOffer.getVpPolicyId());


            log.debug("\t --> Generating ReqE2e and VerifierNonce");
            String generateNonce = generateNonce();
            verifyProfile.setId(UUID.randomUUID().toString());
            VerifyProcess process = verifyProfile.getProfile().getProcess();
            ReqE2e reqE2e = process.getReqE2e();
            KeyPairInterface keyPair = generateEcKeyPair(reqE2e.getCurve());
            generateReqE2e(reqE2e, generateNonce, keyPair);
            process.setReqE2e(reqE2e);
            process.setVerifierNonce(generateNonce);
            String encodedSessionKey = encodedSessionKey((ECPrivateKey) keyPair.getPrivateKey());
            log.debug("\t --> Retrieving verifier DID Document");
            VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();
            DidDocument verifierDidDoc = didDocService.getDidDocument(verifierInfo.getDid());

            log.debug("\t --> Generating Verify Profile Proof");
            verifyProfile.setProof(generatePreProof(verifierDidDoc));
            verifyProfile.setProof(generateProof(verifyProfile));

            log.debug("\t --> Saving VP Profile and E2E information");
            VpProfileSave(verifyProfile, transaction.getId());
            SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());


            transactionService.saveSubTransaction(SubTransaction.builder()
                    .transactionId(transaction.getId())
                    .step(lastSubTransaction.getStep() + 1)
                    .type(SubTransactionType.REQUEST_PROFILE)
                    .status(SubTransactionStatus.COMPLETED)
                    .build());

            e2EQueryService.save(E2e.builder()
                    .transactionId(transaction.getId())
                    .curve(reqE2e.getCurve())
                    .cipher(reqE2e.getCipher())
                    .padding(reqE2e.getPadding())
                    .nonce(reqE2e.getNonce())
                    .sessionKey(encodedSessionKey)
                    .build());

            log.debug("*** Finished requestProfile ***");

            return RequestProfileResDto.builder()
                    .profile(verifyProfile)
                    .txId(transaction.getTxId())
                    .build();

        } catch (OpenDidException e){
            log.error("OpenDidException occurred during RequestingProfile: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred during RequestingProfile: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_PROFILE);
        }
    }

    private void setVerifierProviderDetail(VerifyProfile verifyProfile) {
        InnerVerifyProfile profile = new InnerVerifyProfile();
        ProviderDetail providerDetail = new ProviderDetail();
        VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();
        providerDetail.setCertVcRef(verifierInfo.getCertificateUrl());
        providerDetail.setDid(verifierInfo.getDid());
        providerDetail.setRef(verifierInfo.getServerUrl());
        providerDetail.setName(verifierInfo.getName());
        profile.setVerifier(providerDetail);
        verifyProfile.setProfile(profile);

    }


    /**
     * Requests verification of a Verifiable Presentation (VP).
     * This method decrypts the VP, verifies it against the stored profile, and stores the verified VP.
     * It's the core of the verification process.
     *
     * @param requestVerifyReqDto The request data for verification, including encrypted VP and transaction ID
     * @return RequestVerifyResDto The response data after verification, confirming successful verification
     * @throws OpenDidException If there's an error in the verification process, such as invalid data or failed verification
     */
    @Override
    public RequestVerifyResDto requestVerify(RequestVerifyReqDto requestVerifyReqDto) {
        try {
            log.info("=== Starting requestVerify ===");
            log.debug("\t --> Retrieving transaction information and last sub-transaction");
            Transaction transaction = transactionService.findTransactionByTxId(requestVerifyReqDto.getTxId());
            SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());

            log.debug("\t --> Validating transaction and last sub-transaction");
            validateTransaction(transaction, lastSubTransaction);

            log.debug("\t --> Retrieving VP Profile and verifying AuthType");
            VerifyProfile findProfile = findProfile(requestVerifyReqDto.getTxId());


            log.debug("\t --> if AccE2e proof exists, verify it");
            if(Objects.nonNull(requestVerifyReqDto.getAccE2e().getProof())){
                verifyAccE2eProof(requestVerifyReqDto.getAccE2e());
            }

            log.debug("\t --> Decrypting VP and verifying AuthType");
            String serverNonce = findProfile.getProfile().getProcess().getVerifierNonce();
            VerifiablePresentation verifiablePresentation = decryptVp(requestVerifyReqDto, serverNonce);

            log.debug("\t --> Verifying AuthType");
            authTypeValid(findProfile, verifiablePresentation);

            log.debug("\t --> Validating Nonce");
            nonceValid(verifiablePresentation.getVerifierNonce(), serverNonce);

            log.debug("\t --> Verifying VP");
            VerifyVp(verifiablePresentation, findProfile.getProfile().getFilter());

            log.debug("\t --> Saving VP data and updating transaction status");
            String vpData = verifiablePresentation.toJson();
            vpSubmitRepository.save(VpSubmit.builder()
                    .transactionId(transaction.getId())
                    .vp(vpData)
                    .holderDid(verifiablePresentation.getHolder())
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
        } catch (OpenDidException e){
            log.error("OpenDidException occurred during requestVerify: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred during requestingVerify: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_VERIFY);
        }
    }

    /**
     * Validates if the provided client nonce matches the server nonce.
     * This method is crucial for ensuring the integrity and freshness of the verification request.
     *
     * @param clientNonce The nonce received from the client to be validated
     * @param serverNonce The nonce generated or stored on the server
     * @throws OpenDidException if the decoded client nonce does not match the decoded server nonce
     */
    private void nonceValid(String clientNonce, String serverNonce) {

        byte[] decodeClientNonce = BaseMultibaseUtil.decode(clientNonce);
        byte[] decodedServerNonce = BaseMultibaseUtil.decode(serverNonce);
        if(!Arrays.equals(decodeClientNonce, decodedServerNonce)){
            throw new OpenDidException(ErrorCode.INVALID_NONCE);
        }
    }

    /**
     * Confirms the verification of a VP.
     * This method is typically called after the verification process to retrieve the verified claims.
     *
     * @param confirmVerifyReqDto The request data for confirming verification, including the offer ID
     * @return ConfirmVerifyResDto The response data after confirmation, including verified claims if successful
     */
    @Override
    public ConfirmVerifyResDto confirmVerify(ConfirmVerifyReqDto confirmVerifyReqDto) {
        try {

            Transaction transaction = transactionService.findTransactionByOfferId(confirmVerifyReqDto.getOfferId());
            VpSubmit vpSubmit = vpSubmitRepository.findByTransactionId(transaction.getId());
            List<Claim> returnClaims = new ArrayList<>();
            if(vpSubmit == null){
                return ConfirmVerifyResDto.builder()
                        .result(false)
                        .build();
            } else {
                VpOffer vpOffer = vpOfferRepository.findByOfferId(confirmVerifyReqDto.getOfferId())
                        .orElseThrow(() -> new OpenDidException(ErrorCode.VP_OFFER_NOT_FOUND));
                OfferType offerType = OfferType.valueOf(vpOffer.getOfferType());
                if(offerType.equals(OfferType.VerifyProofOffer)){
                    Claim claim = new Claim();
                    String zkpClaim = "{"
                            + "\"caption\": \"ZKP Verification Result\","
                            + "\"code\": \"ZkpTestResult's Codes\","
                            + "\"format\": \"plain\","
                            + "\"hideValue\": false,"
                            + "\"type\": \"text\","
                            + "\"value\": \"Successful\""
                            + "}";

                    claim.fromJson(zkpClaim);
                    returnClaims.add(claim);

                    return ConfirmVerifyResDto.builder()
                            .result(true)
                            .claims(returnClaims)
                            .build();
                } else if(offerType.equals(OfferType.VerifyOffer)){
                    String vp = vpSubmit.getVp();
                    VerifiablePresentation verifiablePresentation = new VerifiablePresentation();
                    verifiablePresentation.fromJson(vp);
                    List<VerifiableCredential> verifiableCredentials =  verifiablePresentation.getVerifiableCredential();
                    verifiableCredentials.forEach(vc -> {
                        List<@Valid Claim> claims = vc.getCredentialSubject().getClaims();
                        returnClaims.addAll(claims);
                    });
                    return ConfirmVerifyResDto.builder()
                            .result(true)
                            .claims(returnClaims)
                            .build();
                } else {
                    throw new OpenDidException(ErrorCode.VP_OFFER_NOT_FOUND);
                }

            }
        } catch (OpenDidException e){
            log.error("OpenDidException occurred during ConfirmVerify: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred during ConfirmVerify: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_CONFIRM_VERIFY);
        }

    }

    @Override
    public ProofRequestResDto requestProofRequestProfile(RequestProfileReqDto requestProfileReqDto)  {
        try {

            log.info("=== Starting requestProofRequestProfile ===");
            log.debug("\t --> ZkpProofRequestProfile - Retrieving transaction information");
            Transaction transaction = findTransactionByRequestDto(requestProfileReqDto);
            VpOffer vpOffer = findVpOfferByTransaction(transaction);

            log.debug("\t --> Retrieving ProofRequestProfile by policyId in VP Offer");
            ProofRequestProfile proofRequestProfile = getProofRequestProfileFromPolicy(vpOffer.getVpPolicyId());

            ReqE2e reqE2e = proofRequestProfile.getProfile().getReqE2e();
            String generateNonce = generateNonce();
            KeyPairInterface keyPair = generateEcKeyPair(reqE2e.getCurve());
            generateReqE2e(reqE2e, generateNonce, keyPair);
            proofRequestProfile.setId(UUID.randomUUID().toString());
            proofRequestProfile.getProfile().setReqE2e(reqE2e);
            String encodedSessionKey = encodedSessionKey((ECPrivateKey) keyPair.getPrivateKey());

            log.debug("\t --> ZkpProofRequestProfile - Retrieving verifier DID Document");
            VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();
            DidDocument verifierDidDoc = didDocService.getDidDocument(verifierInfo.getDid());
            log.debug("\t --> ZkpProofRequestProfile - Generating Verify Profile Proof");

            proofRequestProfile.setProof(generatePreProof(verifierDidDoc));
            proofRequestProfile.setProof(generateZkpProof(proofRequestProfile));

            ZkpVpProfileSave(proofRequestProfile, transaction.getId());
            SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());

            transactionService.saveSubTransaction(SubTransaction.builder()
                    .transactionId(transaction.getId())
                    .step(lastSubTransaction.getStep() + 1)
                    .type(SubTransactionType.REQUEST_PROFILE)
                    .status(SubTransactionStatus.COMPLETED)
                    .build());

            e2EQueryService.save(E2e.builder()
                    .transactionId(transaction.getId())
                    .curve(reqE2e.getCurve())
                    .cipher(reqE2e.getCipher())
                    .padding(reqE2e.getPadding())
                    .nonce(reqE2e.getNonce())
                    .sessionKey(encodedSessionKey)
                    .build());

            log.debug("\t --> Generating Proof");
            return ProofRequestResDto.builder()
                    .proofRequestProfile(proofRequestProfile)
                    .txId(transaction.getTxId())
                    .build();


        } catch (OpenDidException e){
            log.error("OpenDidException occurred during RequestingProoofRequestProfile: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred during RequestingProfile: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_PROOF_REQUEST_PROFILE);
        }
    }

    @Override
    public RequestVerifyResDto requestVerifyProof(RequestVerifyProofReqDto requestVerifyProofReqDto) {

        log.info("=== Starting requestVerifyProof ===");
        log.debug("\t --> Retrieving transaction information and last sub-transaction");
        Transaction transaction = transactionService.findTransactionByTxId(requestVerifyProofReqDto.getTxId());
        SubTransaction lastSubTransaction = transactionService.findLastSubTransaction(transaction.getId());
        validateTransaction(transaction, lastSubTransaction);

        log.debug("\t --> Retrieving VP Profile and verifying AuthType");
        ProofRequestProfile findProofRequestProfile = findProofProfile(requestVerifyProofReqDto.getTxId());

        log.debug("\t --> if AccE2e proof exists, verify it");
        if(Objects.nonNull(requestVerifyProofReqDto.getAccE2e().getProof())){
            verifyAccE2eProof(requestVerifyProofReqDto.getAccE2e());
        }
        org.omnione.did.zkp.datamodel.proof.Proof proof = decryptProof(requestVerifyProofReqDto);

        BigInteger proofNonce = new BigInteger(requestVerifyProofReqDto.getNonce());

        List<ProofVerifyParam> proofVerifyParams = getProofVerifyParams(proof.getIdentifiers());

        VerifyProof(proof, proofNonce, findProofRequestProfile.getProfile().getProofRequest(), proofVerifyParams);


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

        log.debug("*** Finished requestProofVerify ***");
        return RequestVerifyResDto.builder()
                .txId(requestVerifyProofReqDto.getTxId())
                .build();
    }

    private LinkedList<ProofVerifyParam> getProofVerifyParams(List<Identifiers> identifiers) {

        LinkedList<ProofVerifyParam> proofVerifyParams = new LinkedList<>();
        for (Identifiers id : identifiers) {
            org.omnione.did.zkp.datamodel.schema.CredentialSchema zkpCredSchema = storageService.getZKPCredential(id.getSchemaId());
            CredentialDefinition zkpCredentialDefinition = storageService.getZKPCredentialDefinition(id.getCredDefId());
            ProofVerifyParam proofVerifyParam = new ProofVerifyParam.Builder()
                    .setSchema(zkpCredSchema)
                    .setCredentialDefinition(zkpCredentialDefinition)
                    .build();
            proofVerifyParams.add(proofVerifyParam);
        }
        return proofVerifyParams;
    }



    private void VerifyProof(org.omnione.did.zkp.datamodel.proof.Proof proof, BigInteger proofNonce, ProofRequest proofRequest, List<ProofVerifyParam> proofVerifyParams)  {
        ZkpProofManager zkpProofManager = new ZkpProofManager();
        try {
            zkpProofManager.verifyProof(proof, proofNonce, proofRequest, proofVerifyParams);
        } catch (ZkpException e) {
            log.error("ZkpException occurred during VerifyProof: {}", e.getErrorCode());
            log.error("ZkpException occurred during VerifyProof: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.FAILED_TO_VERIFY_PROOF);
        }

    }

    private org.omnione.did.zkp.datamodel.proof.Proof decryptProof(RequestVerifyProofReqDto requestVerifyProofReqDto) {
        Transaction transaction = transactionService.findTransactionByTxId(requestVerifyProofReqDto.getTxId());
        E2e e2e = e2EQueryService.findByTransactionId(transaction.getId());
        SymmetricCipherType e2eCipher =  SymmetricCipherType.fromDisplayName(e2e.getCipher());
        SymmetricPaddingType e2ePadding = SymmetricPaddingType.fromDisplayName(e2e.getPadding());

        byte[] decodeEncProof = BaseMultibaseUtil.decode(requestVerifyProofReqDto.getEncProof());
        byte[] decodeIv = BaseMultibaseUtil.decode(requestVerifyProofReqDto.getAccE2e().getIv());
        byte[] sharedSecretKey = generateSharedSecretKey(requestVerifyProofReqDto.getAccE2e(), e2e);
        byte[] mergeSharedSecretAndNonce = mergeSharedSecretAndNonce(sharedSecretKey, e2e.getNonce(), e2eCipher);
        byte[] decryptedData = decrypt(decodeEncProof, mergeSharedSecretAndNonce, decodeIv, e2eCipher, e2ePadding);
        String decodeProofStr = new String(decryptedData, StandardCharsets.UTF_8);
        return new Gson().fromJson(decodeProofStr, org.omnione.did.zkp.datamodel.proof.Proof.class);
    }

    private ReqE2e setReqE2e(ZkpProofRequest zkpProofRequest) {
        ReqE2e reqE2e = new ReqE2e();

        reqE2e.setCurve(zkpProofRequest.getCurve().toString());
        reqE2e.setCipher(zkpProofRequest.getCipher().toString());
        reqE2e.setPadding(zkpProofRequest.getPadding().toString());
        return reqE2e;
    }

    private ProofRequestProfile getProofRequestProfileFromPolicy(String vpPolicyId) throws IOException {
        Policy policy = policyRepository.findByPolicyId(vpPolicyId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_NOT_FOUND));

        String zkpProfileId = policy.getPolicyProfileId();

        ZkpPolicyProfile zkpPolicyProfile = zkpPolicyProfileRepository.findByProfileId(zkpProfileId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.ZKP_POLICY_PROFILE_NOT_FOUND));
        String jsonZkpProfile = objectMapper.writeValueAsString(zkpPolicyProfile);
        ProofRequestProfile proofRequestProfile = objectMapper.readValue(jsonZkpProfile, ProofRequestProfile.class);
        setInnerProfile(proofRequestProfile, zkpPolicyProfile.getZkpProofRequestId());


        return proofRequestProfile;

    }

    private void setInnerProfile(ProofRequestProfile proofRequestProfile, Long zkpProofRequestId) {
        ZkpProofRequest zkpProofRequest = zkpProofRequestRepository.findById(zkpProofRequestId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.ZKP_PROOF_REQUEST_NOT_FOUND));
        ReqE2e reqE2e = setReqE2e(zkpProofRequest);
        ProofRequest proofRequest = new ProofRequest();
        BigInteger verifierNonce = new BigIntegerUtil().createRandomBigInteger(ZkpCryptoConstants.LARGE_NONCE);

        GsonWrapper gson = new GsonWrapper();

        if (zkpProofRequest.getRequestedAttributes() != null && !zkpProofRequest.getRequestedAttributes().isEmpty()) {
            String requestedAttributesJson = zkpProofRequest.getRequestedAttributes();
            ProofRequest tempProofReq = gson.fromJson("{\"requestedAttributes\":" + requestedAttributesJson + "}", ProofRequest.class);
            proofRequest.setRequestedAttributes(tempProofReq.getRequestedAttributes());
        }

        if (zkpProofRequest.getRequestedPredicates() != null && !zkpProofRequest.getRequestedPredicates().isEmpty()) {
            String requestedPredicatesJson = zkpProofRequest.getRequestedPredicates();
            ProofRequest tempProofReq = gson.fromJson("{\"requestedPredicates\":" + requestedPredicatesJson + "}", ProofRequest.class);
            proofRequest.setRequestedPredicates(tempProofReq.getRequestedPredicates());
        }

        proofRequest.setNonce(verifierNonce);
        proofRequest.setName(zkpProofRequest.getName());
        proofRequest.setVersion(zkpProofRequest.getVersion());

        ZkpInnerVerifyProfile innerVerifyProfile = new ZkpInnerVerifyProfile();
        ProviderDetail providerDetail = new ProviderDetail();
        VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();
        providerDetail.setCertVcRef(verifierInfo.getCertificateUrl());
        providerDetail.setDid(verifierInfo.getDid());
        providerDetail.setRef(verifierInfo.getServerUrl());
        providerDetail.setName(verifierInfo.getName());
        innerVerifyProfile.setVerifier(providerDetail);
        innerVerifyProfile.setProofRequest(proofRequest);
        innerVerifyProfile.setReqE2e(reqE2e);

        proofRequestProfile.setProfile(innerVerifyProfile);

        log.debug("ProofRequest: {}", proofRequestProfile.toJson());
    }



    ////ZKP TEST END /////
    /**
     * Validates the AuthType of a VerifiablePresentation against the VerifyProfile.
     * This ensures that the authentication method used in the VP matches the requirements set in the profile.
     *
     * @param findProfile The VerifyProfile to check against
     * @param verifiablePresentation The VerifiablePresentation to validate
     * @throws OpenDidException If the AuthType is invalid or doesn't meet the profile requirements
     */
    private void authTypeValid(VerifyProfile findProfile, VerifiablePresentation verifiablePresentation) {
        /**
         * @HACK Currently, only the logic for PIN or BIO and unrestricted authentication has been implemented.
         * Implementation for other types of authentication may be necessary in the future as needed.
         */
        int authType = findProfile.getProfile().getProcess().getAuthType();
        if(VerifyAuthType.fromInteger(authType).equals(VerifyAuthType.NO_RESTRICTIONS_AUTHENTICATION)){
            return;
        }
        log.debug("AuthType: {}", VerifyAuthType.fromInteger(authType));

        if(VerifyAuthType.fromInteger(authType).equals(VerifyAuthType.PIN_OR_BIO)){
            List<VerifyAuthType> authTypes = Arrays.asList(VerifyAuthType.BIO, VerifyAuthType.PIN);
            String resAuthType = verifiablePresentation.getProof().getVerificationMethod().split("#")[1].toUpperCase();
            VerifyAuthType verifyAuthTypeRes = VerifyAuthType.valueOf(resAuthType);
            log.debug("verifyAuthTypeRes: {}", verifyAuthTypeRes);
            if (!authTypes.contains(verifyAuthTypeRes)) {
                throw new OpenDidException(ErrorCode.INVALID_AUTH_TYPE);
            }
        }
    }

    /**
     * Finds a VerifyProfile by transaction ID.
     * This method retrieves the stored profile associated with a specific transaction.
     *
     * @param txId The transaction ID to search for
     * @return VerifyProfile The found VerifyProfile
     * @throws OpenDidException If the transaction or profile is not found, or if there's an error parsing the profile
     */
    private VerifyProfile findProfile(String txId) {
        try {
            Transaction transaction = transactionService.findTransactionByTxId(txId);
            if (transaction == null) {
                throw new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND);
            }
            VpProfile vpProfile = vpProfileRepository.findTop1ByTransactionIdOrderByCreatedAtDesc(transaction.getId())
                    .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(vpProfile.getVpProfile(), VerifyProfile.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse VP profile for txId: {}", txId, e);
            throw new OpenDidException(ErrorCode.VP_PROFILE_PARSE_ERROR);
        }
    }

    private ProofRequestProfile findProofProfile(String txId) {
        try {
            Transaction transaction = transactionService.findTransactionByTxId(txId);
            if (transaction == null) {
                throw new OpenDidException(ErrorCode.TRANSACTION_NOT_FOUND);
            }
            // Retrieve the ProofProfile from the database
            VpProfile vpProfile = vpProfileRepository.findByTransactionId(transaction.getId())
                    .orElseThrow(() -> new OpenDidException(ErrorCode.VP_PROFILE_NOT_FOUND));

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(vpProfile.getVpProfile(), ProofRequestProfile.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse VP profile for txId: {}", txId, e);
            throw new OpenDidException(ErrorCode.VP_PROFILE_PARSE_ERROR);
        }
    }

    /**
     * Retrieves and validates the policy for a VP Offer request.
     *
     * @param requestOfferReqDto The request data for VP Offer
     * @return JsonNode The policy as a JsonNode
     * @throws OpenDidException If the policy is not found
     */
    private VerifyOfferResult getPolicyAndValidate(RequestOfferReqDto requestOfferReqDto) throws JsonProcessingException {
        //분기 타입
        Optional<Policy> policy = policyRepository.findByPolicyId(requestOfferReqDto.getPolicyId());
        if (policy.isEmpty()) {
            log.error("Policy not found for payloadId: {}", requestOfferReqDto.getPolicyId());
            throw new OpenDidException(ErrorCode.VP_POLICY_NOT_FOUND);
        }
        String policyId = policy.get().getPolicyId();

        Optional<Payload> payload = payloadRepository.findByPayloadId(policy.get().getPayloadId());
        if (payload.isEmpty()) {
            log.error("Payload not found for payloadId: {}", requestOfferReqDto.getPolicyId());
            throw new OpenDidException(ErrorCode.VP_PAYLOAD_NOT_FOUND);
        }
        VerifyOfferPayload verifyOfferPayload = policyToVerifyOfferPayload(payload);

        return VerifyOfferResult.builder()
                .vpPolicyId(policyId)
                .offerPayload(verifyOfferPayload)
                .build();
    }

    /**
     * Creates and saves a new transaction.
     *
     * @return Transaction The created transaction
     */
    private Transaction createAndSaveTransaction() {
        return transactionService.insertTransaction(Transaction.builder()
                .type(TransactionType.VP_SUBMIT)
                .txId(UUID.randomUUID().toString())
                .status(TransactionStatus.PENDING)
                .expired_at(transactionService.retrieveTransactionExpiredTime())
                .build());
    }

    /**
     * Creates and saves a new sub-transaction.
     *
     * @param transactionId The ID of the parent transaction
     */
    private void createAndSaveSubTransaction(Long transactionId) {
        transactionService.saveSubTransaction(SubTransaction.builder()
                .transactionId(transactionId)
                .step(1)
                .type(SubTransactionType.REQUEST_OFFER)
                .status(SubTransactionStatus.COMPLETED)
                .build());
    }

    /**
     * Creates and saves a VP Offer.
     *
     * @param transactionId      The ID of the associated transaction
     * @param vpOfferId          The ID of the VP Offer
     * @param verifyOfferResult  The result of the offer verification
     * @param extractedPayload   The extracted payload
     * @param validUntil         The expiration time of the offer
     */
    private void SaveVpOffer(Long transactionId, String vpOfferId,
                             VerifyOfferResult verifyOfferResult, String extractedPayload, Instant validUntil) {
        vpOfferQueryService.insertVpOffer(VpOffer.builder()
                .transactionId(transactionId)
                .offerId(vpOfferId)
                .device(verifyOfferResult.getOfferPayload().getDevice())
                .service(verifyOfferResult.getOfferPayload().getService())
                .vpPolicyId(verifyOfferResult.getVpPolicyId())
                .offerType(verifyOfferResult.getOfferPayload().getType().toString())
                .payload(extractedPayload)
                .validUntil(validUntil)
                .build());
    }


    /**
     * Verifies a Verifiable Presentation.
     *
     * @param verifiablePresentation The VerifiablePresentation to verify
     * @param filter The filter to apply during verification
     * @throws OpenDidException If the verification fails
     */
    private void VerifyVp(VerifiablePresentation verifiablePresentation, Filter filter) {
        VpManager vpManager = new VpManager();
        DidDocument holderDid = storageService.findDidDoc(verifiablePresentation.getHolder());
        List<VerifiableCredential> verifiableCredentials = verifiablePresentation.getVerifiableCredential();
        verifiableCredentials.forEach(vc -> {
            DidDocument issuerDidDoc = storageService.findDidDoc(vc.getIssuer().getId());
            VpVerifyParam vpVerifyParam = new VpVerifyParam(holderDid, issuerDidDoc);
            vpVerifyParam.setFilter(filter);
            try {
                vpManager.verifyPresentation(verifiablePresentation, vpVerifyParam);
            } catch (CoreException e) {
                throw new OpenDidException(ErrorCode.VP_VERIFY_ERROR);
            }
        });
    }

    /**
     * Decrypts a Verifiable Presentation.
     *
     * @param requestVerifyReqDto The request data containing the encrypted VP
     * @param verifierNonce The nonce used for verification
     * @return VerifiablePresentation The decrypted Verifiable Presentation
     * @throws OpenDidException If decryption fails
     */
    private VerifiablePresentation decryptVp(RequestVerifyReqDto requestVerifyReqDto, String verifierNonce) {
        Transaction transaction = transactionService.findTransactionByTxId(requestVerifyReqDto.getTxId());
        E2e e2e = e2EQueryService.findByTransactionId(transaction.getId());
        SymmetricCipherType e2eCipher =  SymmetricCipherType.fromDisplayName(e2e.getCipher());
        SymmetricPaddingType e2ePadding = SymmetricPaddingType.fromDisplayName(e2e.getPadding());

        byte[] decodeEncVp = BaseMultibaseUtil.decode(requestVerifyReqDto.getEncVp());
        byte[] decodeIv = BaseMultibaseUtil.decode(requestVerifyReqDto.getAccE2e().getIv());
        byte[] sharedSecretKey = generateSharedSecretKey(requestVerifyReqDto.getAccE2e(), e2e);
        byte[] mergeSharedSecretAndNonce = mergeSharedSecretAndNonce(sharedSecretKey, verifierNonce, e2eCipher);
        byte[] decryptedData = decrypt(decodeEncVp, mergeSharedSecretAndNonce, decodeIv, e2eCipher, e2ePadding);
        String decodeVP = new String(decryptedData, StandardCharsets.UTF_8);
        log.info("Decoded VP: {}", decodeVP);
        VerifiablePresentation verifiablePresentation = new VerifiablePresentation();
        verifiablePresentation.fromJson(decodeVP);
        return verifiablePresentation;
    }

    /**
     * Verifies the proof of an AccE2e object.
     *
     * @param accE2e The AccE2e object to verify
     * @throws OpenDidException If the verification fails
     */
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
            BaseCryptoUtil.verifySignature(publicKeyByKeyId.getPublicKeyMultibase(), proof.getProofValue(),
            BaseDigestUtil.generateHash(accE2eString.getBytes(StandardCharsets.UTF_8)), EccCurveType.SECP_256_R1);

        } catch (CommonSdkException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        } catch (Exception e) {
            throw new OpenDidException(ErrorCode.ACC_E2E_ERROR);
        }

    }

    /**
     * Saves a VP Profile.
     *
     * @param verifyProfile The VerifyProfile to save
     * @param txId The ID of the associated transaction
     * @throws OpenDidException If saving fails
     */
    private void VpProfileSave(VerifyProfile verifyProfile, Long txId)  {
        VpProfile vpProfile = new VpProfile();
        vpProfile.setProfileId(verifyProfile.getId());
        vpProfile.setTransactionId(txId);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String verifyProfileToJson = objectMapper.writeValueAsString(verifyProfile);
            vpProfile.setVpProfile(verifyProfileToJson);
            vpProfileRepository.save(vpProfile);
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.VERIFY_PROFILE_PARSE_ERROR);
        }
    }

    /**
     * Saves a VP Profile.
     *
     * @param proofRequestProfile The VerifyProfile to save
     * @param txId The ID of the associated transaction
     * @throws OpenDidException If saving fails
     */
    private void ZkpVpProfileSave(ProofRequestProfile proofRequestProfile, Long txId)  {
        VpProfile vpProfile = new VpProfile();
        vpProfile.setProfileId(proofRequestProfile.getId());
        vpProfile.setTransactionId(txId);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String verifyProfileToJson = objectMapper.writeValueAsString(proofRequestProfile);
            vpProfile.setVpProfile(verifyProfileToJson);
            vpProfileRepository.save(vpProfile);
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.PROOF_REQUEST_PROFILE_PARSE_ERROR);
        }
    }

    /**
     * Finds a VP Offer by transaction.
     *
     * @param transaction The transaction to search by
     * @return VpOffer The found VP Offer
     */
    private VpOffer findVpOfferByTransaction(Transaction transaction) {
        return vpOfferQueryService.findByTransactionId(transaction.getId());
    }

    /**
     * Generates ReqE2e data.
     *
     * @param reqE2e The ReqE2e object to populate
     * @param verifierNonce The nonce for the verifier
     * @param keyPair The key pair for E2E encryption
     * @throws OpenDidException If generation fails
     */
    private void generateReqE2e(ReqE2e reqE2e, String verifierNonce, KeyPairInterface keyPair)  {
       try {
           ECPublicKey publicKey = (ECPublicKey) keyPair.getPublicKey();
           byte[] encodedPublicKey = BaseCryptoUtil.compressPublicKey(publicKey.getEncoded(), EccCurveType.SECP_256_R1);
           reqE2e.setPublicKey(MultiBaseUtils.encode(encodedPublicKey, MultiBaseType.base58btc));
           reqE2e.setNonce(verifierNonce);
       } catch (CryptoException e) {
           throw new OpenDidException(ErrorCode.CRYPTO_ERROR);
       }

    }

    /**
     * Finds a transaction by the request DTO.
     * Validates the transaction status and expiration time.
     *
     * @param requestProfileReqDto The request DTO
     * @return Transaction The found transaction
     * @throws OpenDidException If the transaction is invalid or expired
     */
    private Transaction findTransactionByRequestDto(RequestProfileReqDto requestProfileReqDto) {
        Transaction transaction = null;
        if(requestProfileReqDto.getTxId() == null || requestProfileReqDto.getTxId().isEmpty()){
            transaction = transactionService.findTransactionByOfferId(requestProfileReqDto.getOfferId());
        } else {
            transaction = transactionService.findTransactionByTxId(requestProfileReqDto.getTxId());
        }

        if(transaction.getStatus() != TransactionStatus.PENDING){
            throw new OpenDidException(ErrorCode.TRANSACTION_INVALID);
        }

        Instant now = Instant.now();
        if(now.isAfter(transaction.getExpired_at())){
            throw new OpenDidException(ErrorCode.TRANSACTION_EXPIRED);
        }
        return transaction;
    }

    /**
     * Retrieves a VerifyProfile from a policy.
     *
     * @param policyId The ID of the policy
     * @return VerifyProfile The retrieved VerifyProfile
     * @throws OpenDidException If the policy is not found or cannot be parsed
     */
    private VerifyProfile getVerifyProfileFromPolicy(String policyId) throws IOException {
        Policy policy = policyRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_NOT_FOUND));

        String policyProfileId = policy.getPolicyProfileId();

        PolicyProfile policyProfile = policyProfileRepository.findByPolicyProfileId(policyProfileId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_PROFILE_NOT_FOUND));

        String jsonProfile = objectMapper.writeValueAsString(policyProfile);
        VerifyProfile verifyProfile = objectMapper.readValue(jsonProfile, VerifyProfile.class);


        setVerifierProviderDetail(verifyProfile);
        setVpFilter(verifyProfile, policyProfile.getFilterId());
        setVpProcess(verifyProfile, policyProfile.getProcessId());

        return verifyProfile;

    }

    /**
     * Generates a nonce.
     *
     * @return String The generated nonce
     */
    private String generateNonce() {
        return BaseMultibaseUtil.encode(BaseCryptoUtil.generateNonce(16), MultiBaseType.base64);
    }

    /**
     * Validates a transaction and its sub-transaction.
     *
     * @param transaction The transaction to validate
     * @param subTransaction The sub-transaction to validate
     * @throws OpenDidException If the transaction or sub-transaction is invalid
     */
    private void validateTransaction(Transaction transaction, SubTransaction subTransaction) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new OpenDidException(ErrorCode.TRANSACTION_INVALID);
        }
        Instant now = Instant.now();
        if(now.isAfter(transaction.getExpired_at())){
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

    /**
     * Calculates the expiration time for an offer.
     *
     * @param validSeconds The number of seconds the offer should be valid
     * @return Instant The calculated expiration time
     */
    private Instant offerTimeValidSeconds(Integer validSeconds) {
        return Instant.now().plusSeconds(validSeconds);
    }

    /**
     * Converts a policy payload to a VerifyOfferPayload.
     *
     * @param payload The policy payload
     * @return VerifyOfferPayload The converted VerifyOfferPayload
     * @throws OpenDidException If parsing fails
     */
    private VerifyOfferPayload policyToVerifyOfferPayload(Optional<Payload> payload) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<String> endpoints = objectMapper.readValue(payload.get().getEndpoints(), ArrayList.class);
        Instant validUntil = offerTimeValidSeconds(payload.get().getValidSecond());

        return VerifyOfferPayload.builder()
                .service(payload.get().getService())
                .device(payload.get().getDevice())
                .mode(PresentMode.fromProfileMode(payload.get().getMode()))
                .endpoints(endpoints)
                .locked(payload.get().isLocked())
                .validUntil(validUntil.toString())
                .type(payload.get().getOfferType())
                .build();

    }

    /**
     * Generates a proof for a VerifyProfile.
     *
     * @param verifyProfile The VerifyProfile to generate a proof for
     * @return Proof The generated proof
     * @throws OpenDidException If JSON processing fails
     */
    private Proof generateProof(VerifyProfile verifyProfile)  {
        try {
            String serializedAndSortedProfile = serializeAndSort(verifyProfile);
            byte[] signatureBytes = walletService.generateCompactSignature("assert", serializedAndSortedProfile);
            Proof proof = new Proof();
            proof.setType(verifyProfile.getProof().getType());
            proof.setCreated(verifyProfile.getProof().getCreated());
            proof.setProofPurpose(verifyProfile.getProof().getProofPurpose());
            proof.setVerificationMethod(verifyProfile.getProof().getVerificationMethod());
            proof.setProofValue(BaseMultibaseUtil.encode(signatureBytes, MultiBaseType.base58btc));
            return proof;
        } catch (CommonSdkException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }

    }

    /**
     * Generates a proof for a ProofRequestProfile.
     *
     * @param proofRequestProfile The ProofRequestProfile to generate a proof for
     * @return Proof The generated proof
     * @throws OpenDidException If JSON processing fails
     */
    private Proof generateZkpProof(ProofRequestProfile proofRequestProfile)  {
        try {
            String serializedAndSortedProfile = serializeAndSort(proofRequestProfile);
            byte[] signatureBytes = walletService.generateCompactSignature("assert", serializedAndSortedProfile);
            Proof proof = new Proof();
            proof.setType(proofRequestProfile.getProof().getType());
            proof.setCreated(proofRequestProfile.getProof().getCreated());
            proof.setProofPurpose(proofRequestProfile.getProof().getProofPurpose());
            proof.setVerificationMethod(proofRequestProfile.getProof().getVerificationMethod());
            proof.setProofValue(BaseMultibaseUtil.encode(signatureBytes, MultiBaseType.base58btc));
            return proof;
        } catch (CommonSdkException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }

    }

    /**
     * Generates a preliminary proof for a VerifyProfile.
     *
     * @param verifierDidDoc The verifier's DID document
     * @return Proof The generated preliminary proof
     */
    private Proof generatePreProof(DidDocument verifierDidDoc) {
        Proof proof = new Proof();
        proof.setType(ProofType.SECP256R1_SIGNATURE_2018.getRawValue());
        proof.setCreated(DateTimeUtil.getCurrentUTCTimeString());
        proof.setProofPurpose(ProofPurpose.ASSERTION_METHOD.toString());
        String verificationMethod = getVerificationMethod(verifierDidDoc);
        proof.setVerificationMethod(verificationMethod);
        return proof;
    }

    /**
     * Generates a shared secret key using the AccE2e public key and the stored E2e session key.
     * This is a crucial step in the end-to-end encryption process.
     *
     * @param accE2e The AccE2e object containing the public key
     * @param e2e The E2e object containing the session key
     * @return byte[] The generated shared secret key
     */
    private byte[] generateSharedSecretKey(AccE2e accE2e, E2e e2e) {
        log.debug("\t--> Generate SharedSecretKey");
        byte[] decodePubKey = BaseMultibaseUtil.decode(accE2e.getPublicKey());
        byte[] decodePriKey = BaseMultibaseUtil.decode(e2e.getSessionKey());

        return BaseCryptoUtil.generateSharedSecret(decodePubKey, decodePriKey,
                EccCurveType.fromValue(e2e.getCurve()));
    }
    /**
     * Merges a shared secret key with a nonce.
     * This method is used to create a unique encryption key for each session.
     *
     * @param sharedSecretKey The shared secret key
     * @param nonce The nonce to merge
     * @param cipherType The type of cipher to use
     * @return byte[] The merged result, which can be used as an encryption key
     */
    private byte[] mergeSharedSecretAndNonce(byte[] sharedSecretKey, String nonce, SymmetricCipherType cipherType) {

        return BaseCryptoUtil.mergeSharedSecretAndNonce(sharedSecretKey,
                BaseMultibaseUtil.decode(nonce),
                cipherType);
    }

    /**
     * Encodes a session key.
     * This method prepares the private key for storage or transmission in a secure format.
     *
     * @param privateKey The private key to encode
     * @return String The encoded session key
     */
    private String encodedSessionKey(ECPrivateKey privateKey) {
        return BaseMultibaseUtil.encode(privateKey.getEncoded());
    }

    /**
     * Retrieves the verification method from a verifier's DID document.
     * This method is used to determine how the verifier's identity should be verified.
     *
     * @param verifierDidDoc The verifier's DID document
     * @return String The verification method
     */
    private String getVerificationMethod(DidDocument verifierDidDoc)
    {
        String version = verifierDidDoc.getVersionId();
        VerificationMethod verificationMethod = BaseCoreDidUtil.getVerificationMethod(verifierDidDoc, ProofPurpose.ASSERTION_METHOD.toKeyId());

        return verifierDidDoc.getId() + "?versionId=" + version + "#" + verificationMethod.getId();
    }

    /**
     * Generates a key pair for E2E encryption.
     * This method creates a new key pair for secure end-to-end communication.
     *
     * @param curve The curve to use for the key pair generation
     * @return EcKeyPair The generated key pair
     */
    private EcKeyPair generateEcKeyPair(String curve) {
        EccCurveType eccCurveType = EccCurveType.fromValue(curve);
        return (EcKeyPair) BaseCryptoUtil.generateKeyPair(eccCurveType);
    }

    private void setVpFilter(VerifyProfile verifyProfile, Long filterId) throws IOException {
        Optional<VpFilter> byFilterId = vpFilterRepository.findByFilterId(filterId);
        if(byFilterId.isEmpty()){
            throw new OpenDidException(ErrorCode.VP_FILTER_NOT_FOUND);
        }
        String filterString = objectMapper.writeValueAsString(byFilterId);
        log.debug("FilterString: {}", filterString);
        CredentialSchema credentialSchema = objectMapper.readValue(filterString, CredentialSchema.class);
        log.debug("CredentialSchema: {}", credentialSchema.toJson());
        Filter filter = new Filter();
        filter.setCredentialSchemas(List.of(credentialSchema));
        verifyProfile.getProfile().setFilter(filter);

    }

    private void setVpProcess(VerifyProfile verifyProfile, Long processId)  {
        Optional<VpProcess> vpProcessOptional = vpProcessRepository.findById(processId);
        if (vpProcessOptional.isEmpty()) {
            throw new OpenDidException(ErrorCode.VP_PROCESS_NOT_FOUND);
        }

        VpProcess vpProcess = vpProcessOptional.get();

        ProcessDTO processDTO = ProcessDTO.fromEntity(vpProcess);
        String tempNonce = "tempNonce";
        ProcessDTO.ReqE2e reqE2e = new ProcessDTO.ReqE2e();
        processDTO.setVerifierNonce(tempNonce);
        reqE2e.setNonce(tempNonce);
        reqE2e.setCurve(vpProcess.getCurve());
        reqE2e.setCipher(vpProcess.getCipher());
        reqE2e.setPadding(vpProcess.getPadding());
        reqE2e.setPublicKey("tempPubKey");
        processDTO.setReqE2e(reqE2e);

        Process process = processDTO.toVerifyProcessEntity();
        String processString = new GsonWrapper().toJson(process);

        log.debug("ProcessString with generated nonce: {}", processString);

        VerifyProcess verifyProcess = new VerifyProcess();
        verifyProcess.fromJson(processString);

        log.debug("verifyProcess: {}", verifyProcess.toJson());
        verifyProfile.getProfile().setProcess(verifyProcess);
    }




}
