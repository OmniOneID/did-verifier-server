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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.datamodel.data.AccEcdh;
import org.omnione.did.base.datamodel.data.Candidate;
import org.omnione.did.base.datamodel.data.DidAuth;
import org.omnione.did.base.datamodel.data.EcdhReqData;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.db.domain.CertificateVc;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.*;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.crypto.exception.CryptoException;
import org.omnione.did.crypto.keypair.EcKeyPair;
import org.omnione.did.data.model.did.Proof;
import org.omnione.did.data.model.enums.did.ProofPurpose;
import org.omnione.did.data.model.enums.did.ProofType;
import org.omnione.did.data.model.vc.VerifiableCredential;
import org.omnione.did.verifier.v1.api.EnrollFeign;
import org.omnione.did.verifier.v1.api.dto.*;
import org.omnione.did.verifier.v1.dto.EnrollEntityReqDto;
import org.omnione.did.verifier.v1.dto.EnrollEntityResDto;
import org.springframework.stereotype.Service;

import java.security.interfaces.ECPrivateKey;
import java.util.Arrays;

/**
 * The EnrollEntityServiceImpl class manages the entity enrollment process.
 * This service coordinates multiple steps including proposing enrollment,
 * performing ECDH key exchange, requesting enrollment, and confirming enrollment.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EnrollEntityServiceImpl implements EnrollEntityService {
    private final EnrollFeign enrollFeign;
    private final FileWalletService walletManagerInterface;
    private final CertificateVcQueryService certificateVcQueryService;


    /**
     * Enrolls an entity through a multistep process:
     * 1. Proposes entity enrollment
     * 2. Performs ECDH key exchange
     * 3. Requests entity enrollment
     * 4. Confirms entity enrollment
     *
     * This method orchestrates the entire enrollment flow, handling cryptographic
     * operations, API calls, and data persistence.
     *
     * @return EnrollEntityResDto An empty response indicating successful enrollment.
     */
    @Override
    public EnrollEntityResDto enrollEntity() {
        try {
            log.debug("=== Starting Enroll Entity ===");

            log.debug("\t--> 1. propose Enroll Entity");
            ProposeEnrollEntityApiResDto proposeResponse = proposeEnrollEntity();
            String txId = proposeResponse.getTxId();
            String authNonce = proposeResponse.getAuthNonce();

            log.debug("\t--> 2. request ECDH");
            EccCurveType eccCurveType = EccCurveType.SECP_256_R1;
            log.debug("\t\t--> generate Tmp Keypair");
            EcKeyPair ecKeyPair = (EcKeyPair) BaseCryptoUtil.generateKeyPair(eccCurveType);
            log.debug("\t\t--> generate ReqEcdh");
            String clientNonce = BaseMultibaseUtil.encode(BaseCryptoUtil.generateNonce(16));
            EcdhReqData reqData = generateReqData(ecKeyPair, eccCurveType, clientNonce);
            log.debug("\t\t--> request ECDH");
            RequestEcdhApiResDto ecdhResponse = requestEcdh(txId, reqData);

            log.debug("\t--> 3. request Enroll Entity");
            log.debug("\t\t--> generate DID Auth");
            DidAuth didAuth = generateDidAuth(authNonce);
            log.debug("\t\t--> request Enroll Entity");
            RequestEnrollEntityApiResDto enrollEntityResponse = requestEnrollEntity(txId, didAuth);
            log.debug("\t\t--> decrypt VC");
            VerifiableCredential vc = decryptVc((ECPrivateKey) ecKeyPair.getPrivateKey(),
                    ecdhResponse.getAccEcdh(), enrollEntityResponse, clientNonce);

            log.debug("\t--> 4. confirm Enroll Entity");
            ConfirmEnrollEntityApiResDto confirmResponse = confirmEnrollEntity(txId, vc.getId());

            log.debug("\t\t--> save VC to DB");
            certificateVcQueryService.save(CertificateVc.builder()
                    .vc(vc.toJson())
                    .build());

            log.debug("== Finish");
            return EnrollEntityResDto.builder()
                    .build();
        } catch (Exception e) {
            log.error("An unknown error occurred while enrolling entity", e);
            throw new OpenDidException(ErrorCode.FAILED_TO_ISSUE_CERTIFICATE_VC);
        }
    }

    /**
     * Initiates the enrollment process by sending a proposal to the enrollment service.
     * This is the first step in the enrollment flow.
     *
     * @return ProposeEnrollEntityApiResDto containing transaction ID and authentication nonce
     */
    private ProposeEnrollEntityApiResDto proposeEnrollEntity() {
        ProposeEnrollEntityApiReqDto request = ProposeEnrollEntityApiReqDto.builder()
                .id(RandomUtil.generateMessageId())
                .build();
        return enrollFeign.proposeEnrollEntityApi(request);
    }

    /**
     * Requests ECDH (Elliptic Curve Diffie-Hellman) key exchange.
     * This step is crucial for establishing a secure communication channel.
     *
     * @param txId    The transaction ID obtained from the proposal step
     * @param reqEcdh The ECDH request data containing necessary cryptographic information
     * @return RequestEcdhApiResDto containing the server's response to the ECDH request
     */
    private RequestEcdhApiResDto requestEcdh(String txId, EcdhReqData reqEcdh) {
        RequestEcdhApiReqDto request = RequestEcdhApiReqDto.builder()
                .id(RandomUtil.generateMessageId())
                .txId(txId)
                .reqEcdh(reqEcdh)
                .build();
        return enrollFeign.requestEcdh(request);
    }

    /**
     * Generates the ECDH request data, including public key, nonce, and proof.
     * This data is essential for the key exchange process.
     *
     * @param publicKey   The EC public key
     * @param curveType   The elliptic curve type used
     * @param clientNonce A client-generated nonce for added security
     * @return EcdhReqData containing all necessary ECDH request information
     * @throws OpenDidException if cryptographic operations fail
     */
    private EcdhReqData generateReqData(EcKeyPair publicKey, EccCurveType curveType, String clientNonce) {
        try {
            Candidate candidate = Candidate.builder()
                    .ciphers(Arrays.asList(SymmetricCipherType.values()))
                    .build();

            String verificationMethod = "did:omn:verifier?versionId=1#keyagree";
            Proof proof = BaseCryptoUtil.generateProof(ProofType.SECP256R1_SIGNATURE_2018,
                    ProofPurpose.KEY_AGREEMENT, verificationMethod);

            EcdhReqData reqData = EcdhReqData.builder()
                    .client("did:omn:verifier")
                    .clientNonce(clientNonce)
                    .curve(curveType)
                    .publicKey(publicKey.getBase58CompreessPubKey())
                    .candidate(candidate)
                    .proof(proof)
                    .build();

            proof.setProofValue(signData(reqData, "keyagree"));

            return reqData;
        } catch (CryptoException e) {
            throw new OpenDidException(ErrorCode.CRYPTO_ERROR);
        }
    }

    /**
     * Sends the actual enrollment request to the enrollment service.
     * This step uses the DID authentication generated from the previous steps.
     *
     * @param txId    The transaction ID for this enrollment process
     * @param didAuth The DID authentication object
     * @return RequestEnrollEntityApiResDto containing the enrollment response
     */
    private RequestEnrollEntityApiResDto requestEnrollEntity(String txId, DidAuth didAuth) {
        RequestEnrollEntityApiReqDto request = RequestEnrollEntityApiReqDto.builder()
                .id(RandomUtil.generateMessageId())
                .txId(txId)
                .didAuth(didAuth)
                .build();

        return enrollFeign.requestEnrollEntityApi(request);
    }

    /**
     * Generates the DID authentication object required for the enrollment request.
     * This includes creating a proof of authentication.
     *
     * @param authNonce The authentication nonce received in the proposal step
     * @return DidAuth object containing the authentication proof
     */
    private DidAuth generateDidAuth(String authNonce) {

        String verificationMethod = "did:omn:verifier?versionId=1#auth";

        Proof proof = BaseCryptoUtil.generateProof(ProofType.SECP256R1_SIGNATURE_2018,
                ProofPurpose.AUTHENTICATION, verificationMethod);

        DidAuth didAuth = DidAuth.builder()
                .authNonce(authNonce)
                .did("did:omn:verifier")
                .proof(proof)
                .build();

        proof.setProofValue(signData(didAuth, "auth"));
        return didAuth;
    }


    /**
     * Decrypts the Verifiable Credential (VC) received in the enrollment response.
     * This method uses the ECDH shared secret and other cryptographic parameters
     * to securely decrypt the VC.
     *
     * @param privateKey           The EC private key used for decryption
     * @param accEcdh              The accepted ECDH parameters from the server
     * @param enrollEntityResponse The enrollment response containing the encrypted VC
     * @param clientNonce          The client nonce used in the ECDH process
     * @return VerifiableCredential The decrypted and parsed VC
     */
    private VerifiableCredential decryptVc(ECPrivateKey privateKey, AccEcdh accEcdh, RequestEnrollEntityApiResDto enrollEntityResponse, String clientNonce) {
        byte[] compressedPublicKey = BaseMultibaseUtil.decode(accEcdh.getPublicKey());
        byte[] sharedSecret = BaseCryptoUtil.generateSharedSecret(compressedPublicKey, privateKey.getEncoded(), EccCurveType.SECP_256_R1);
        byte[] mergeNonce = BaseCryptoUtil.mergeNonce(clientNonce, accEcdh.getServerNonce());
        byte[] mergeSharedSecretAndNonce = BaseCryptoUtil.mergeSharedSecretAndNonce(sharedSecret, mergeNonce, accEcdh.getCipher());

        byte[] iv = BaseMultibaseUtil.decode(enrollEntityResponse.getIv());

        byte[] decrypt = BaseCryptoUtil.decrypt(
                enrollEntityResponse.getEncVc(),
                mergeSharedSecretAndNonce,
                iv,
                accEcdh.getCipher(),
                accEcdh.getPadding()
        );

        String jsonVc = new String(decrypt);
        VerifiableCredential vc = new VerifiableCredential();
        vc.fromJson(jsonVc);

        return vc;
    }

    /**
     * Confirms the entity enrollment by sending a confirmation request to the enrollment service.
     * This is the final step in the enrollment process.
     *
     * @param txId The transaction ID for this enrollment process
     * @param vcId The ID of the Verifiable Credential received
     * @return ConfirmEnrollEntityApiResDto containing the confirmation response
     */
    private ConfirmEnrollEntityApiResDto confirmEnrollEntity(String txId, String vcId) {
        ConfirmEnrollEntityApiReqDto request = ConfirmEnrollEntityApiReqDto.builder()
                .id(RandomUtil.generateMessageId())
                .txId(txId)
                .vcId(vcId)
                .build();

        return enrollFeign.confirmEnrollEntityApi(request);
    }

    /**
     * Signs the given data using the specified key ID.
     * This method is used to generate signatures for various parts of the enrollment process.
     *
     * @param source The data to be signed
     * @param keyId  The ID of the key to use for signing
     * @return String The base58-encoded signature
     * @throws OpenDidException if JSON serialization fails
     */
    private String signData(Object source, String keyId) {
        try {
            String serializeSource = JsonUtil.serializeAndSort(source);
            byte[] signature =  walletManagerInterface.generateCompactSignature(keyId, serializeSource);
            return BaseMultibaseUtil.encode(signature);
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }
    }
}
