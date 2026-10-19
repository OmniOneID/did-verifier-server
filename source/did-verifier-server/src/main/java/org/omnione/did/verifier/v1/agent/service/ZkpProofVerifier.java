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
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.common.service.StorageService;
import org.omnione.did.zkp.core.manager.ZkpProofManager;
import org.omnione.did.zkp.datamodel.definition.CredentialDefinition;
import org.omnione.did.zkp.datamodel.proof.Identifiers;
import org.omnione.did.zkp.datamodel.proof.Proof;
import org.omnione.did.zkp.datamodel.proof.verifyparam.ProofVerifyParam;
import org.omnione.did.zkp.datamodel.proofrequest.ProofRequest;
import org.omnione.did.zkp.datamodel.schema.CredentialSchema;
import org.omnione.did.zkp.exception.ZkpException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Verifies a decrypted ZKP proof against the proof request stored for the transaction.
 * The nonce used for verification is always the one issued and stored by the verifier.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class ZkpProofVerifier {

    private final StorageService storageService;

    /**
     * Verifies the proof with the stored proof request.
     *
     * @param proof        Decrypted proof submitted by the holder.
     * @param proofRequest Proof request stored for the transaction.
     * @param clientNonce  Nonce sent with the request.
     * @throws OpenDidException if the nonce does not match the stored nonce or the proof is not valid.
     */
    public void verify(Proof proof, ProofRequest proofRequest, String clientNonce) {
        BigInteger serverNonce = proofRequest.getNonce();
        nonceValid(clientNonce, serverNonce);

        List<ProofVerifyParam> proofVerifyParams = getProofVerifyParams(proof.getIdentifiers());

        boolean verified;
        try {
            verified = new ZkpProofManager().verifyProof(proof, serverNonce, proofRequest, proofVerifyParams);
        } catch (ZkpException e) {
            log.error("ZkpException occurred during VerifyProof: {}", e.getErrorCode());
            log.error("ZkpException occurred during VerifyProof: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.FAILED_TO_VERIFY_PROOF);
        }

        if (!verified) {
            log.error("ZKP proof verification returned false");
            throw new OpenDidException(ErrorCode.FAILED_TO_VERIFY_PROOF);
        }
    }

    /**
     * Validates that the nonce sent by the client matches the nonce stored in the proof request.
     *
     * @param clientNonce Nonce sent with the request.
     * @param serverNonce Nonce stored in the proof request.
     * @throws OpenDidException if the nonces are missing or do not match.
     */
    private void nonceValid(String clientNonce, BigInteger serverNonce) {
        if (serverNonce == null || clientNonce == null) {
            throw new OpenDidException(ErrorCode.INVALID_NONCE);
        }
        try {
            if (!serverNonce.equals(new BigInteger(clientNonce))) {
                throw new OpenDidException(ErrorCode.INVALID_NONCE);
            }
        } catch (NumberFormatException e) {
            throw new OpenDidException(ErrorCode.INVALID_NONCE);
        }
    }

    private List<ProofVerifyParam> getProofVerifyParams(List<Identifiers> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            throw new OpenDidException(ErrorCode.FAILED_TO_VERIFY_PROOF);
        }

        LinkedList<ProofVerifyParam> proofVerifyParams = new LinkedList<>();
        for (Identifiers id : identifiers) {
            CredentialSchema zkpCredSchema = storageService.getZKPCredential(id.getSchemaId());
            CredentialDefinition zkpCredentialDefinition = storageService.getZKPCredentialDefinition(id.getCredDefId());
            ProofVerifyParam proofVerifyParam = new ProofVerifyParam.Builder()
                    .setSchema(zkpCredSchema)
                    .setCredentialDefinition(zkpCredentialDefinition)
                    .build();
            proofVerifyParams.add(proofVerifyParam);
        }
        return proofVerifyParams;
    }
}
