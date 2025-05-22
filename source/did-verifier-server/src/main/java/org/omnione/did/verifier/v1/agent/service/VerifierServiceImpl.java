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
            }
        } catch (OpenDidException e){
            log.error("OpenDidException occurred during ConfirmVerify: {}", e.getErrorCode().getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception occurred during ConfirmVerify: {}", e.getMessage(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_CONFIRM_VERIFY);
        }

    }

    ////ZKP /////
    @Override
    public ProofRequestResDto requestProofRequestProfile(RequestProfileReqDto requestProfileReqDto)  {
        try {

            //1. txId로 transaction 조회, offerId 조회
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


        //Dev Data
        //DB에서 해당 값을 찾아와서 조회


        //TestData
        //BigInteger verifierNonce = new BigIntegerUtil().createRandomBigInteger(ZkpCryptoConstants.LARGE_NONCE);
//        ProofRequest proofRequest = ZkpProofManager.requestProofReq(
//                "mdl",
//                "11",
//                ZkpTestConstants.getProofRequestAttribute(),
//                ZkpTestConstants.getProofRequestPredicate(),
//                verifierNonce
//        );

//        String jsonStr = "{\n" +
//                "  \"proofRequestProfile\": {\n" +
//                "    \"id\": \"f55044ba-fa69-4bba-91e3-38442043d6bc\",\n" +
//                "    \"type\": \"ProofRequestProfile\",\n" +
//                "    \"title\": \"zkp Test를 위한 임시 프로파일 입니다.\",\n" +
//                "    \"description\": \"하드코딩 용 임시 프로파일입니다..\",\n" +
//                "    \"encoding\": \"UTF-8\",\n" +
//                "    \"language\": \"ko\",\n" +
//                "    \"profile\": {\n" +
//                "      \"verifier\": {\n" +
//                "        \"did\": \"did:omn:verifier\",\n" +
//                "        \"certVcRef\": \"http://10.48.17.129:8092/verifier/api/v1/certificate-vc\",\n" +
//                "        \"name\": \"verifier\",\n" +
//                "        \"description\": \"verifier\",\n" +
//                "        \"ref\": \"http://10.48.17.129:8092/swagger-ui/index.html#/\"\n" +
//                "      },\n" +
//                "      \"reqE2e\": {\n" +
//                "        \"nonce\": \"msG81SiTzYpOZLEolWB7t2w\",\n" +
//                "        \"curve\": \"Secp256r1\",\n" +
//                "        \"publicKey\": \"z28MKkcmEAMzvUUwDDmtSTD8DduyDq3iZCz9UgZBp1AADd\",\n" +
//                "        \"cipher\": \"AES-256-CBC\",\n" +
//                "        \"padding\": \"PKCS5\"\n" +
//                "      }\n" +
//                "    },\n" +
//                "    \"proof\": {\n" +
//                "      \"type\": \"Secp256r1Signature2018\",\n" +
//                "      \"created\": \"2025-05-14T19:29:42.742437Z\",\n" +
//                "      \"verificationMethod\": \"did:omn:verifier?versionId=1#assert\",\n" +
//                "      \"proofPurpose\": \"assertionMethod\",\n" +
//                "      \"proofValue\": \"z3r3GNPRnfzLUK9HLGGjkNSVdM6FtJnmB56DRxMaDmFtkgFN1ifTn5n45NgiTiwRY7QbSDf62WxU66q8WqiyfKT5i4\"\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";
//        ProofRequestResDto proofRequestResDto = null;
//        try {
//            proofRequestResDto = JsonUtil.deserializeFromJson(jsonStr, ProofRequestResDto.class);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        proofRequestResDto.getProofRequestProfile().getProfile().setProofRequest(proofRequest);
//        return proofRequestResDto;

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
        log.debug("TestLog## proof = " + proof.toJson());
        log.debug("TestLog## proofNonce = " + proofNonce);
        log.debug("TestLog## proofVerifyParams = " + GsonWrapper.getGson().toJson(proofVerifyParams));
        log.debug("TestLog## proofRequest = " + GsonWrapper.getGson().toJson(findProofRequestProfile.getProfile().getProofRequest()));



        VerifyProof(proof, proofNonce, findProofRequestProfile.getProfile().getProofRequest(), proofVerifyParams);


        vpSubmitRepository.save(VpSubmit.builder()
                .transactionId(transaction.getId())
                .vp("ZKP Submit")
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

        //@@TODO: 블록체인에서 가져와야함
        LinkedList<ProofVerifyParam> proofVerifyParams = new LinkedList<>();
        for (Identifiers id : identifiers) {
//            String schemaStr = " {\n" +
//                    "  \"id\": \"did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0\",\n" +
//                    "  \"name\": \"mdl\",\n" +
//                    "  \"version\": \"1.0\",\n" +
//                    "  \"attrNames\": [\n" +
//                    "    \"zkpsex\",\n" +
//                    "    \"zkpbirth\",\n" +
//                    "    \"zkpasort\",\n" +
//                    "    \"zkpaddr\"\n" +
//                    "  ],\n" +
//                    "  \"tag\": \"Tag1\"\n" +
//                    "}";

            org.omnione.did.zkp.datamodel.schema.CredentialSchema zkpCredSchema = storageService.getZKPCredential(id.getSchemaId());


//
//            String defStr = "{\n" +
//                    "  \"id\": \"did:omn:NcYxiDXkpYi6ov5FcYDi1e:3:CL:did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0:Tag1\",\n" +
//                    "  \"schemaId\": \"did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0\",\n" +
//                    "  \"ver\": \"1.0\",\n" +
//                    "  \"type\": \"CL\",\n" +
//                    "  \"value\": {\n" +
//                    "    \"primary\": {\n" +
//                    "      \"n\": \"92775526040564561686692065084198526388182965052935282919213101966498168538455855403734866233185600697532637603374230608927392371217905764468993212867731342552070099117167560805363206167456133168830781321739842895351453160406795952557662895731883300837340365480915043225592351933938942912364477352882497616416646372252852067477605128561602232477187158306658612482751740703029577426260284551923737989897429965794096108222439264266670479609111336321250438916920879772933864404383615945046782416756930307564650579911154642192284532062451046180714550385568468878314573777872613155664035129576807096185472992202354597905819\",\n" +
//                    "      \"z\": \"43686372030520392901178678013277112308031276158148263133617645738733963962925874432344700742933184123929740127097742436041327182133633470202629679087590546787926089432071524845069707402923869182183581741625438069117159494007536799307629412567833377726001845802962737533720815907767966889320359488037682194017415542933748559674774290361739895146466067254544401115360044814465854246952418724764642790006483867687841067502397371664368533570528972257370102346454405444991834575631058665973366598480162991787829962060646243784193748283099983375596372908276139502233494457699605465545001843799796123920628981257914111811630\",\n" +
//                    "      \"s\": \"39491553553325946539036179395177575610930217913368362160347502982041940121706543209205186925681250567611675997590774809752630525574063423133320070432886319376183480691305633507102081514167099568358039876608130741160386462798377414350237911601755513207619995428437591776226243275239699523970798094411343685159221553039937609603890319734835294875668136599689581212976229075236912743003175036347254957449633666699707818439488140637653218747180804521775493065155031445310228917054586989621251840607782259531431660294127013413142063014600722772823125540472754038974263690373845146352943477593046775364357731961008773490108\",\n" +
//                    "      \"r\": {\n" +
//                    "        \"zkpsex\": \"63481253450392554723608972411575005353287352671358074059874458708321784291990070631742274361146662429677996237887710528429101353867177653237387725779675355492241669431034004739006579372119086533300507445114734526431884769837981313133170254299666297943714456648772252377546453915318126402352735310221036865359445618414353698009079559168739353595297460032737716624261976636345366965072321696528244286455011570614684260880980068859109175378580410474397336219872660146655198680529730697616075045756014975847185114041949481311512559463123683049896310820978594264280249117685830215348589235922922548151777670245460766091013\",\n" +
//                    "        \"zkpbirth\": \"25492686680706889388200627923395471731213053911684876307056664110131827137759797904990694456540954634667302639654569180616864154053493935623314882002961062178699081718983316726936380310419914276772045020961144677857315351282712340304413435684591811833420762241461295584876786068035814199365118454976349870021873139384539738840286028689171198683207077331354856526222101819994625486869549239857610476749658917897794097987070174317563263197254285527902318873800612498317669218977073927198097137160260572712521719152184854553804845328745599414408526747765286967047832419953529714528374996267419756950750059757094827264086\",\n" +
//                    "        \"zkpasort\": \"87981414142670211420620140660570255424387117894396315960845333105530513446326256913129261496756609051210081386287551874039757615258264875788886541612613098422212109117791460074493711622016082156014116478461079237405311410580110979858300292664779076957010848535680995010803745570363514780281237152308374113154701817040274385293007590324643492459638911662267937597207479277426540930675307856883744168517370448791758540975269888013981166525571346917340382866815735011530275712483420723728217657450206015236190587598949478523742550574090511660379858398804289273419443168022080965286188924450331071023862315218086451679153\",\n" +
//                    "        \"zkpaddr\": \"85659701271520010822062760833607562707144958499644193537806866105044265684127605307492471441203578699119805878723056573695785716066784342101039831341835413060762425369840545695469336721917816131021909352885905549796081111671838116382575836206586869181647004416070155294875541557989875791829691653687914791799028959619817913473760807139260483083541942167535092664303757692048296246396057538996572420064677404716877244675505523722495618874448508392649616623019032146905518229874779365693626432575328049947421737434473696960221688257600746869839204698250671515721646063981784810627447576396052175760782593481154456727317\",\n" +
//                    "        \"masterSecret\": \"90768072314619617876859174927500335388484978421537439274050814305492431971509613515329693754472418219115012435420294045878342097669777527447263346252574086097489879939416271856146116575580360295891095487444705890397783110612114218129614016782432232858037993768551688170777726678327868776878403206427432535256442400578782003874178275515366068507827373239378559823325791790838303886640361310872115160425697881353556605894869263774406284266670697020256430748382299480709944961734227583245525893733239568120074778715475230401571564263966133767450313903185116942488335392804354763595904654596673155887924500027606394823033\"\n" +
//                    "      },\n" +
//                    "      \"rctxt\": \"36603345067563337549097037553020584544190852890343616984670941818945307595231711546544197134988824857656913286370035890586600848184672593292275430944567014241513950920918714055316471065518864044800354423159776835619239889122667760680473999087917018537846301507100674925413558317136397392305434025640769634477561121105331902936914880670136751471958051029880174410171693650138501333300700069452721023409107569379418110354392792710890398565018278733537372560589057798681467140920960248230051672136392338922370270566920369588191926452952169410214523821737016087168952915573991065156796313661924879193414874696252725241075\"\n" +
//                    "    }\n" +
//                    "  },\n" +
//                    "  \"tag\": \"Tag1\"\n" +
//                    "}";
            //CredentialDefinition credentialDefinition = new Gson().fromJson(defStr, CredentialDefinition.class);
            CredentialDefinition zkpCredentialDefinition = storageService.getZKPCredentialDefinition(id.getCredDefId());
            log.debug("CredentialDefinition: {}", zkpCredentialDefinition.toJson());
            log.debug("CredentialSchema: {}", zkpCredSchema.toJson());
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

        reqE2e.setCurve(zkpProofRequest.getCurve());
        reqE2e.setCipher(zkpProofRequest.getCipher());
        reqE2e.setPadding(zkpProofRequest.getPadding());
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
        log.debug("ZkpPolicyProfile: {}", proofRequestProfile.toJson());
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



//    @Override
//    public ZkpResponse requestVerifyProofsample(HashMap<String, Object> map) {
//        org.opendid.zkp.zkptestcore.datamodel.zkp.Proof proof = ZkpGsonWrapper.getGson().fromJson(ZkpGsonWrapper.getGson().toJson(map.get("proof")), org.opendid.zkp.zkptestcore.datamodel.zkp.Proof.class);
//        BigInteger verifierNonce = ZkpGsonWrapper.getGson().fromJson(ZkpGsonWrapper.getGson().toJson(map.get("proofNonce")), BigInteger.class);
//
//        List<ProofVerifyParam> proofVerifyParams = new LinkedList<>();
//
//        for (Identifiers identifiers : proof.getIdentifiers()) {
//            String schemaStr = loadZKPData("credentialSchema");
//            org.opendid.zkp.zkptestcore.datamodel.zkp.CredentialSchema schema = new Gson().fromJson(schemaStr, org.opendid.zkp.zkptestcore.datamodel.zkp.CredentialSchema.class);
//            String defStr = loadZKPData("credentialDefinition");
//            CredentialDefinition credentialDefinition = new Gson().fromJson(defStr, CredentialDefinition.class);
//
//            ProofVerifyParam proofVerifyParam = new ProofVerifyParam.Builder()
//                    //todo : 메모리에서 load
//                    .setSchema(schema)
//                    .setCredentialDefinition(credentialDefinition)
//                    .build();
//            proofVerifyParams.add(proofVerifyParam);
//        }
//
//        ZkpResponse zkpResponse = new ZkpResponse(ZkpErrorCode.ERR_CODE_ZKP_SUCCESS, "success");
//
//
//
////        try {
////            zkpResponse = new ZkpProofManager().verifyProof(proof, verifierNonce, proofRequest, proofVerifyParams);
////            sdkResponse.setResultCode(response.getErrorCode());
////            sdkResponse.setResultMsg(response.getErrorMessage());
////        } catch (ZkpException e) {
////            sdkResponse.setResultCode(ZkpErrorCode.ERR_CODE_ZKP_FAIL.getCode());
////            sdkResponse.setResultMsg(e.getErrorMsg());
////        }
//
//        return zkpResponse;
//    }


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
            VpProfile vpProfile = vpProfileRepository.findByTransactionId(transaction.getId())
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

        } catch (JsonProcessingException e) {
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
        } catch (JsonProcessingException e) {
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
        } catch (JsonProcessingException e) {
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
