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
import org.omnione.did.base.datamodel.enums.ProofPurpose;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseCoreDidUtil;
import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.common.exception.CommonSdkException;
import org.omnione.did.common.util.DateTimeUtil;
import org.omnione.did.common.util.JsonUtil;
import org.omnione.did.crypto.enums.MultiBaseType;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.Proof;
import org.omnione.did.data.model.did.VerificationMethod;
import org.omnione.did.data.model.enums.did.ProofType;
import org.omnione.did.data.model.profile.verify.VerifyProfile;
import org.omnione.did.verifier.v1.admin.service.VerifierInfoQueryService;
import org.omnione.did.verifier.v1.model.data.ProofRequestProfile;
import org.springframework.stereotype.Service;

/**
 * Proof signing service.
 *
 * Generates proofs using the application server's FileWallet.
 *
 * Design principles:
 * - Proof generation is the application server's responsibility (the signing key is in the FileWallet)
 * - The SDK returns a profile without a proof, and the application adds the proof
 *
 * Main functions:
 * - generatePreProof: Generates proof metadata (without a signature value)
 * - generateProof: Generates a signature for a VerifyProfile
 * - generateZkpProof: Generates a signature for a ZKP ProofRequestProfile
 * - signVerifyProfile: Convenience method that generates a VerifyProfile proof (preProof + proof)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProofSigningService {

    private final FileWalletService walletService;
    private final DidDocService didDocService;
    private final VerifierInfoQueryService verifierInfoQueryService;

    /**
     * Adds a signed proof to a VerifyProfile (convenience method).
     * Combines generatePreProof and generateProof.
     *
     * @param verifyProfile VerifyProfile to add the proof to
     */
    public void signVerifyProfile(VerifyProfile verifyProfile) {
        VerifierInfo verifierInfo =
                verifierInfoQueryService.getVerifierInfo();
        DidDocument verifierDidDoc = didDocService.getDidDocument(verifierInfo.getDid());
        verifyProfile.setProof(generatePreProof(verifierDidDoc));
        verifyProfile.setProof(generateProof(verifyProfile));
    }

    /**
     * Generates proof metadata (without a signature value).
     * The step before signing a VerifyProfile: sets the verification method, timestamp, etc.
     *
     * @param verifierDidDoc Verifier DID Document
     * @return Proof metadata (without proofValue)
     */
    public Proof generatePreProof(DidDocument verifierDidDoc) {
        Proof proof = new Proof();
        proof.setType(ProofType.SECP256R1_SIGNATURE_2018.getRawValue());
        proof.setCreated(DateTimeUtil.getCurrentUTCTimeString());
        proof.setProofPurpose(ProofPurpose.ASSERTION_METHOD.toString());
        proof.setVerificationMethod(getVerificationMethod(verifierDidDoc));
        return proof;
    }

    /**
     * Generates a signature for a VerifyProfile.
     * Serializes the profile, signs it with the FileWallet and sets the proofValue.
     *
     * @param verifyProfile VerifyProfile to sign (with preProof already set)
     * @return Proof containing the signature
     */
    public Proof generateProof(VerifyProfile verifyProfile) {
        try {
            String serializedAndSortedProfile = JsonUtil.serializeAndSort(verifyProfile);
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
     * Generates a signature for a ZKP ProofRequestProfile.
     *
     * @param profile ProofRequestProfile to sign
     * @param preProof Proof metadata
     * @return Proof containing the signature
     */
    public Proof generateZkpProof(ProofRequestProfile profile, Proof preProof) {
        try {
            String serializedAndSortedProfile = JsonUtil.serializeAndSort(profile);
            byte[] signatureBytes = walletService.generateCompactSignature("assert", serializedAndSortedProfile);
            Proof proof = new Proof();
            proof.setType(preProof.getType());
            proof.setCreated(preProof.getCreated());
            proof.setProofPurpose(preProof.getProofPurpose());
            proof.setVerificationMethod(preProof.getVerificationMethod());
            proof.setProofValue(BaseMultibaseUtil.encode(signatureBytes, MultiBaseType.base58btc));
            return proof;
        } catch (CommonSdkException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }
    }

    /**
     * Adds a signed proof to a ZKP ProofRequestProfile (convenience method).
     *
     * @param sdkProfile ProofRequestProfile to add the proof to
     */
    public void signZkpProfile(ProofRequestProfile sdkProfile) {
        VerifierInfo verifierInfo =
                verifierInfoQueryService.getVerifierInfo();
        DidDocument verifierDidDoc = didDocService.getDidDocument(verifierInfo.getDid());
        Proof preProof = generatePreProof(verifierDidDoc);
        sdkProfile.setProof(generateZkpProof(sdkProfile, preProof));
    }

    /**
     * Extracts the VerificationMethod ID.
     * Returns the verificationMethod ID of the assertionMethod key in the DID Document.
     *
     * @param verifierDidDoc Verifier DID Document
     * @return verificationMethod ID (DID#key format)
     */
    public String getVerificationMethod(DidDocument verifierDidDoc) {
        String version = verifierDidDoc.getVersionId();
        VerificationMethod verificationMethod = BaseCoreDidUtil.getVerificationMethod(
                verifierDidDoc, ProofPurpose.ASSERTION_METHOD.toKeyId());
        return verifierDidDoc.getId() + "?versionId=" + version + "#" + verificationMethod.getId();
    }
}
