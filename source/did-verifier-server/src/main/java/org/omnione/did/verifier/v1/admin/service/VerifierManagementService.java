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

package org.omnione.did.verifier.v1.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.constants.UrlConstant;
import org.omnione.did.base.db.constant.VerifierStatus;
import org.omnione.did.base.db.domain.CertificateVc;
import org.omnione.did.base.db.domain.EntityDidDocument;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.db.repository.DidDocumentRepository;
import org.omnione.did.base.db.repository.VerifierInfoRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.omnione.did.base.response.ErrorResponse;
import org.omnione.did.base.util.BaseCoreDidUtil;
import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.common.exception.HttpClientException;
import org.omnione.did.common.util.HttpClientUtil;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.core.data.rest.DidKeyInfo;
import org.omnione.did.core.manager.DidManager;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.enums.did.ProofPurpose;
import org.omnione.did.data.model.enums.vc.RoleType;
import org.omnione.did.data.model.vc.VerifiableCredential;
import org.omnione.did.verifier.v1.admin.constant.EntityStatus;
import org.omnione.did.verifier.v1.admin.dto.*;
import org.omnione.did.verifier.v1.agent.service.CertificateVcQueryService;
import org.omnione.did.verifier.v1.agent.service.DidDocumentQueryService;
import org.omnione.did.verifier.v1.agent.service.EnrollEntityService;
import org.omnione.did.verifier.v1.agent.service.FileWalletService;
import org.omnione.did.verifier.v1.common.dto.EmptyResDto;
import org.omnione.did.verifier.v1.common.service.StorageService;
import org.omnione.did.wallet.key.WalletManagerInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class VerifierManagementService {
    private final VerifierInfoQueryService verifierInfoQueryService;
    private final StorageService storageService;
    private final CertificateVcQueryService certificateVcQueryService;
    private final VerifierInfoRepository verifierInfoRepository;
    private final FileWalletService fileWalletService;
    private final JsonParseService jsonParseService;
    private final VerifierProperty verifierProperty;
    private final DidDocumentQueryService didDocumentQueryService;
    private final DidDocumentRepository didDocumentRepository;
    private final EnrollEntityService entollEntityService;

    @Value("${tas.url}")
    private String TAS;

    public GetVerifierInfoReqDto getVerifierInfo() {
        VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfoOrNull();
        log.debug("\t--> Found VerifierInfo: {}", verifierInfo);

        if (verifierInfo == null || verifierInfo.getStatus() == VerifierStatus.DID_DOCUMENT_REQUIRED) {
            return GetVerifierInfoReqDto.fromEntity(verifierInfo);
        }

        DidDocument didDocument = storageService.findDidDoc(verifierInfo.getDid());
        return GetVerifierInfoReqDto.fromEntity(verifierInfo, didDocument);
    }

    public EmptyResDto createCertificateVc(SendCertificateVcReqDto sendCertificateVcReqDto) {
        byte[] decodedVc = BaseMultibaseUtil.decode(sendCertificateVcReqDto.getCertificateVc());
        log.debug("Decoded VC: {}", new String(decodedVc));

        certificateVcQueryService.save(CertificateVc.builder()
                .vc(new String(decodedVc))
                .build());

        return new EmptyResDto();
    }

    public EmptyResDto updateEntityInfo(SendEntityInfoReqDto sendEntityInfoReqDto) {
        VerifierInfo existedVerifier = verifierInfoQueryService.getVerifierInfoOrNull();

        if (existedVerifier == null) {
            verifierInfoQueryService.save(VerifierInfo.builder()
                    .name(sendEntityInfoReqDto.getName())
                    .did(sendEntityInfoReqDto.getDid())
                    .status(VerifierStatus.ACTIVATE)
                    .serverUrl(sendEntityInfoReqDto.getServerUrl())
                    .certificateUrl(sendEntityInfoReqDto.getCertificateUrl())
                    .build());
        } else {
            existedVerifier.setName(sendEntityInfoReqDto.getName());
            existedVerifier.setDid(sendEntityInfoReqDto.getDid());
            existedVerifier.setServerUrl(sendEntityInfoReqDto.getServerUrl());
            existedVerifier.setCertificateUrl(sendEntityInfoReqDto.getCertificateUrl());
            existedVerifier.setStatus(VerifierStatus.ACTIVATE);
            verifierInfoQueryService.save(existedVerifier);
        }

        return new EmptyResDto();
    }

    /**
     * Register Verifier information.
     *
     * @param registerVerifierInfoReqDto Request data transfer object
     * @return Verifier information
     */
    public VerifierInfoResDto registerVerifierInfo(RegisterVerifierInfoReqDto registerVerifierInfoReqDto) {
        log.debug("=== Starting registerVerifierInfo ===");

        VerifierInfo verifierInfo = verifierInfoQueryService.findVerifierOrNull();
        log.debug("\t--> Found Verifier: {}", verifierInfo);

        if (verifierInfo == null) {
            log.debug("\t--> Verifier is not registered yet. Proceeding with new registration.");
            verifierInfo = VerifierInfo.builder()
                    .name(registerVerifierInfoReqDto.getName())
                    .serverUrl(registerVerifierInfoReqDto.getServerUrl())
                    .certificateUrl(registerVerifierInfoReqDto.getServerUrl() + "/api/v1/certificate-vc")
                    .status(VerifierStatus.DID_DOCUMENT_REQUIRED)
                    .build();
            verifierInfoQueryService.save(verifierInfo);

            log.debug("=== Finished registerCaInfo ===");
            return buildVerifierInfoResponse(verifierInfo);
        }

        if (verifierInfo.getStatus() == VerifierStatus.ACTIVATE) {
            log.error("Verifier is already registered");
            throw new OpenDidException(ErrorCode.VERIFIER_ALREADY_REGISTERED);
        }

        log.debug("\t--> Updating Verifier information");
        verifierInfo.setName(registerVerifierInfoReqDto.getName());
        verifierInfo.setServerUrl(registerVerifierInfoReqDto.getServerUrl());
        verifierInfo.setCertificateUrl(registerVerifierInfoReqDto.getServerUrl() + "/api/v1/certificate-vc");
        verifierInfoRepository.save(verifierInfo);

        log.debug("=== Finished registerVerifierInfo ===");
        return buildVerifierInfoResponse(verifierInfo);
    }

    /**
     * Builds the VerifierInfoResDto VerifierInfo response.
     *
     * @param verifierInfo VerifierInfo entity
     * @return VerifierInfo information response DTO
     */
    private VerifierInfoResDto buildVerifierInfoResponse(VerifierInfo verifierInfo) {
        if (verifierInfo.getStatus() == VerifierStatus.DID_DOCUMENT_REQUIRED
                || verifierInfo.getStatus() == VerifierStatus.DID_DOCUMENT_REQUESTED) {
            return VerifierInfoResDto.fromEntity(verifierInfo);
        }

        log.debug("\t--> Finding TAS DID Document");
        DidDocument didDocument = storageService.findDidDoc(verifierInfo.getDid());
        return VerifierInfoResDto.fromEntity(verifierInfo, didDocument);
    }

    /**
     * Register Verifier DID Document automatically.
     *
     * This method creates a wallet, generates keys, and creates a DID Document.
     *
     * @return Map containing the generated DID Document
     */
    public Map<String, Object> registerVerifierDidDocumentAuto() {
        log.debug("=== Starting registerCaDidDocumentAuto ===");

        // Finding Verifier
        log.debug("\t--> Finding Verifier");
        VerifierInfo existedVerifier = verifierInfoQueryService.getVerifierInfo();
        log.debug("\t--> Found Verifier: {}", existedVerifier);

        // Check Verifier status
        if (existedVerifier.getStatus() != VerifierStatus.DID_DOCUMENT_REQUIRED) {
            if (existedVerifier.getStatus() == VerifierStatus.DID_DOCUMENT_REQUESTED) {
                log.error("Verifier DID Document is already requested");
                throw new OpenDidException(ErrorCode.VERIFIER_DID_DOCUMENT_ALREADY_REQUESTED);
            }
            log.error("Verifier DID Document is already registered");
            throw new OpenDidException(ErrorCode.VERIFIER_DID_DOCUMENT_ALREADY_REGISTERED);
        }

        // Step1: Create Wallet and keys
        WalletManagerInterface walletManager = initializeWalletWithKeys();

        // Step2: Create DID Document
        DidDocument didDocument = createDidDocumentAuto(walletManager);

        log.debug("=== Finished registerCaDidDocumentAuto ===");

        return jsonParseService.parseDidDocToMap(didDocument.toJson());
    }

    /*
     * Generate Verifier wallet and keys.
     */
    public WalletManagerInterface initializeWalletWithKeys() {
        return fileWalletService.initializeWalletWithKeys();
    }

    /**
     * Create DID Document automatically.
     *
     * This method creates a DID Document using the provided wallet manager.
     *
     * @param walletManager Wallet manager
     * @return Created DID Document
     */
    public DidDocument createDidDocumentAuto(WalletManagerInterface walletManager) {
        String did = "did:omn:verifier";

        Map<String, List<ProofPurpose>> purposes = BaseCoreDidUtil.createDefaultProofPurposes();
        List<DidKeyInfo> keyInfos = BaseCoreDidUtil.getDidKeyInfosFromWallet(walletManager, did, purposes);

        DidManager didManager = new DidManager();
        DidDocument unsignedDoc = BaseCoreDidUtil.createDidDocument(didManager, did, did, keyInfos);

        List<String> signingKeys = BaseCoreDidUtil.getSigningKeyIds(purposes);
        DidDocument signedDoc = BaseCoreDidUtil.signAndAddProof(didManager, walletManager, signingKeys);

        return signedDoc;
    }

    /**
     * Request to register CAS DID Document.
     *
     * Note:
     * - Currently, there is no functionality to cancel a DID Document registration request once it has been sent to TAS.
     * - This cancellation feature is planned for future development.
     *
     * @param reqDto Request data transfer object
     * @return Empty response DTO
     */
    public EmptyResDto requestRegisterDid(RequestRegisterDidReqDto reqDto) {
        try {
            log.debug("=== Starting requestRegisterDid ===");

            VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();
            log.debug("\t--> Found CAS: {}", verifierInfo);

            if (verifierInfo.getStatus() != VerifierStatus.DID_DOCUMENT_REQUIRED) {
                if (verifierInfo.getStatus() == VerifierStatus.DID_DOCUMENT_REQUESTED) {
                    log.error("CAS DID Document is already requested");
                    throw new OpenDidException(ErrorCode.VERIFIER_DID_DOCUMENT_ALREADY_REQUESTED);
                }
                log.error("CAS DID Document is already registered");
                throw new OpenDidException(ErrorCode.VERIFIER_DID_DOCUMENT_ALREADY_REGISTERED);
            }

            // Send the register DID request to TAS
            log.debug("\t--> Sending register DID request to TAS");
            EmptyResDto resDto = sendRegisterDid(verifierInfo, reqDto);

            // Update didDocument in the database
            log.debug("\t--> Updating DID Document in the database");
            EntityDidDocument entityDidDoc = didDocumentQueryService.findDidDocumentOrNull();
            if (entityDidDoc == null) {
                entityDidDoc = EntityDidDocument.builder()
                        .didDocument(reqDto.getDidDocument())
                        .build();
            } else {
                entityDidDoc.setDidDocument(reqDto.getDidDocument());
            }
            didDocumentRepository.save(entityDidDoc);

            // Update CAS status
            log.debug("\t--> Updating CAS did and status");
            verifierInfo.setStatus(VerifierStatus.DID_DOCUMENT_REQUESTED);
            verifierInfo.setDid(BaseCoreDidUtil.parseDid(reqDto.getDidDocument()));
            verifierInfoQueryService.save(verifierInfo);

            log.debug("=== Finished requestRegisterDid ===");

            return resDto;
        } catch (OpenDidException e) {
            log.error("Failed to register Verifier DID Document: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to register Verifier DID Document", e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REGISTER_VERIFIER_DID_DOCUMENT);
        }
    }

    private EmptyResDto sendRegisterDid(VerifierInfo verifierInfo, RequestRegisterDidReqDto requestRegisterDidReqDto) {
        String url = TAS + UrlConstant.Tas.ADMIN_V1 + UrlConstant.Tas.REGISTER_DID_PUBLIC;

        String encodedDidDocument = BaseMultibaseUtil.encode(requestRegisterDidReqDto.getDidDocument().getBytes());
        RegisterDidToTaReqDto registerDidToTaReqDto = RegisterDidToTaReqDto.builder()
                .didDoc(encodedDidDocument)
                .name(verifierInfo.getName())
                .serverUrl(verifierInfo.getServerUrl())
                .certificateUrl(verifierInfo.getCertificateUrl())
                .role(RoleType.APP_PROVIDER)
                .build();
        try {
            String request = JsonUtil.serializeToJson(registerDidToTaReqDto);
            return HttpClientUtil.postData(url, request, EmptyResDto.class);
        } catch (HttpClientException e) {
            log.error("HttpClientException occurred while sending register-did-public request: {}", e.getResponseBody(), e);
            ErrorResponse errorResponse = convertExternalErrorResponse(e.getResponseBody());
            throw new OpenDidException(errorResponse);
        } catch (Exception e) {
            log.error("An unknown error occurred while sending register-did-public request", e);
            throw new OpenDidException(ErrorCode.TAS_COMMUNICATION_ERROR);
        }
    }
    /**
     * Converts an external error response string to an ErrorResponse object.
     * This method attempts to parse the given JSON string into an ErrorResponse instance.
     *
     * @param resBody The JSON string representing the external error response
     * @return An ErrorResponse object parsed from the input string
     * @throws OpenDidException with ErrorCode.TAS_UNKNOWN_RESPONSE if parsing fails
     */
    private ErrorResponse convertExternalErrorResponse(String resBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(resBody, ErrorResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse external error response: {}", resBody, e);
            throw new OpenDidException(ErrorCode.TAS_UNKNOWN_RESPONSE);
        }
    }

    /**
     * Request the status of the entity.
     *
     * @return RequestEntityStatusResDto containing the status information
     */
    public RequestEntityStatusResDto requestEntityStatus() {
        log.debug("=== Starting requestEntityStatus ===");

        // Finding CAS
        log.debug("\t--> Finding Verifier");
        VerifierInfo exsitedVerifier = verifierInfoQueryService.getVerifierInfo();

        String did = exsitedVerifier.getDid();
        if (did == null) {
            EntityDidDocument entityDidDocument = didDocumentQueryService.findDidDocument();
            DidDocument didDocument = new DidDocument();
            didDocument.fromJson(entityDidDocument.getDidDocument());
            did = didDocument.getId();
        }

        // Sending request-status request to TAS
        log.debug("\t--> Sending request-status request to TAS");
        RequestEntityStatusResDto requestEntityStatusResDto = sendRequestEntityStatus(did);

        // Update CAS status based on the response
        if (requestEntityStatusResDto.getStatus() == EntityStatus.NOT_REGISTERED) {
            log.debug("\t--> TA has deleted the entity's registration request. Updating CAS status accordingly");
            exsitedVerifier.setStatus(VerifierStatus.DID_DOCUMENT_REQUIRED);
            verifierInfoRepository.save(exsitedVerifier);
        } else if (requestEntityStatusResDto.getStatus() == EntityStatus.CERTIFICATE_VC_REQUIRED) {
            log.debug("\t--> TA has requested a certificate VC. Updating CAS status accordingly");
            exsitedVerifier.setStatus(VerifierStatus.CERTIFICATE_VC_REQUIRED);
            verifierInfoRepository.save(exsitedVerifier);
        }

        log.debug("=== Finished requestEntityStatus ===");

        return requestEntityStatusResDto;
    }

    private RequestEntityStatusResDto sendRequestEntityStatus(String did) {
        String url = TAS + UrlConstant.Tas.ADMIN_V1 + UrlConstant.Tas.REQUEST_ENTITY_STATUS + "?did=" + did;

        try {
            return HttpClientUtil.getData(url, RequestEntityStatusResDto.class);
        } catch (HttpClientException e) {
            log.error("HttpClientException occurred while sending request-status request: {}", e.getResponseBody(), e);
            ErrorResponse errorResponse = convertExternalErrorResponse(e.getResponseBody());
            throw new OpenDidException(errorResponse);
        } catch (Exception e) {
            log.error("An unknown error occurred while sending request-status request", e);
            throw new OpenDidException(ErrorCode.TAS_COMMUNICATION_ERROR);
        }
    }
    public Map<String, Object> enrollEntity() {
        try {
            log.debug("=== Starting enrollEntity ===");
            // Register the entity
            log.debug("\t--> Registering the entity");
            entollEntityService.enrollEntity();

            // Finding Certificate VC
            log.debug("\t--> Finding Certificate VC");
            CertificateVc certificateVc = certificateVcQueryService.findCertificateVc();
            VerifiableCredential verifiableCredential = new VerifiableCredential();
            verifiableCredential.fromJson(certificateVc.getVc());

            log.debug("=== Finished enrollEntity ===");
            return jsonParseService.parseCertificateVcToMap(verifiableCredential.toJson());
        } catch(OpenDidException e) {
            log.error("An OpenDidException occurred while sending requestCertificateVc request", e);
            throw e;
        } catch (Exception e) {
            log.error("An unknown error occurred while sending requestCertificateVc request", e);
            throw new OpenDidException(ErrorCode.FAILED_TO_REQUEST_CERTIFICATE_VC);
        }
    }

}
