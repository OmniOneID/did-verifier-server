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

package org.omnione.did.verifier.v1.agent.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.omnione.did.base.db.domain.E2e;
import org.omnione.did.base.db.repository.E2eRepository;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.agent.config.E2eProperty;
import org.omnione.did.verifier.v1.agent.service.E2EQueryService;
import org.omnione.did.verifier.v1.provider.CryptoHelper;
import org.omnione.did.verifier.v1.provider.EcdhSessionProvider;
import org.omnione.did.verifier.v1.provider.TransactionProvider;
import org.omnione.did.verifier.v1.model.data.KeyPairInfo;
import org.omnione.did.verifier.v1.model.data.ReqE2e;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * E2E session provider adapter.
 * 
 * Implements the SDK's EcdhSessionProvider interface and connects
 * the application's E2E session management logic to the SDK.
 * 
 * Main features:
 * - Create E2E sessions (key pair generation, nonce generation, DB storage)
 * - Retrieve E2E sessions
 * - Delete E2E sessions
 * - Decrypt encrypted data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcdhSessionProviderImpl implements EcdhSessionProvider {

    private final E2EQueryService e2eQueryService;
    private final E2eRepository e2eRepository;
    private final TransactionProvider transactionManager;
    private final E2eProperty e2eProperty;
    private final CryptoHelper cryptoHelper;

    /**
     * Saves an E2E session (for ZKP).
     *
     * Stores the E2E key pair and settings when a ZKP ProofRequestProfile is created.
     *
     * @param txId Transaction ID
     * @param keyPair E2E key pair (publicKey, privateKey, curve)
     * @param reqE2e E2E encryption settings (nonce, cipher, padding)
     * @throws VerifierSdkException if saving fails
     */
    @Override
    public void saveSession(String txId, KeyPairInfo keyPair, ReqE2e reqE2e) {
        try {
            log.debug("Saving E2E session for txId: {}", txId);

            // 1. Look up transaction ID
            Long transactionId = transactionManager.getTransactionId(txId);
            log.debug("Found transactionId: {} for txId: {}", transactionId, txId);

            // 2. Create and save E2e entity
            E2e e2e = E2e.builder()
                    .transactionId(transactionId)
                    .sessionKey(keyPair.getPrivateKey())  // Store privateKey as sessionKey
                    .nonce(reqE2e.getNonce())
                    .curve(reqE2e.getCurve())
                    .cipher(reqE2e.getCipher())
                    .padding(reqE2e.getPadding())
                    .build();

            e2eQueryService.save(e2e);
            log.info("E2E session saved successfully for txId: {}", txId);

        } catch (OpenDidException e) {
            log.error("Failed to save E2E session for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                    "Failed to save E2E session: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error saving E2E session for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                    "Unexpected error saving E2E session: " + e.getMessage());
        }
    }

    /**
     * Creates a new E2E session.
     *
     * @param txId Transaction ID
     * @return ReqE2e (nonce, curve, publicKey, cipher, padding)
     * @throws VerifierSdkException if creation fails
     */
    @Override
    public ReqE2e createSession(String txId) {
        try {
            log.debug("Creating E2E session for txId: {}", txId);

            // 1. Look up transaction ID
            Long transactionId = transactionManager.getTransactionId(txId);
            log.debug("Found transactionId: {} for txId: {}", transactionId, txId);

            // 2. Generate key pair (using CryptoHelper)
            KeyPairInfo keyPair = cryptoHelper.generateKeyPair(e2eProperty.getCurve());
            log.debug("Generated key pair with curve: {}", e2eProperty.getCurve());

            // 3. Generate nonce (using CryptoHelper, 16 bytes)
            String nonce = cryptoHelper.generateNonce(16);
            log.debug("Generated nonce");

            // 4. Create and save E2e entity (privateKey stored as sessionKey)
            E2e e2e = E2e.builder()
                    .transactionId(transactionId)
                    .sessionKey(keyPair.getPrivateKey())  // Already Multibase-encoded
                    .nonce(nonce)                          // Already Multibase-encoded
                    .curve(e2eProperty.getCurve())
                    .cipher(e2eProperty.getCipher())
                    .padding(e2eProperty.getPadding())
                    .build();

            e2eQueryService.save(e2e);
            log.info("E2E session created successfully for txId: {}", txId);

            // 5. Return ReqE2e
            return ReqE2e.builder()
                    .nonce(nonce)
                    .curve(e2eProperty.getCurve())
                    .publicKey(keyPair.getPublicKey())  // Already Multibase-encoded
                    .cipher(e2eProperty.getCipher())
                    .padding(e2eProperty.getPadding())
                    .build();

        } catch (OpenDidException e) {
            log.error("Failed to create E2E session for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                    "Failed to create E2E session: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating E2E session for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                    "Unexpected error creating E2E session: " + e.getMessage());
        }
    }

    /**
     * Retrieves an E2E session.
     *
     * @param txId Transaction ID
     * @return ReqE2e (nonce, curve, publicKey, cipher, padding)
     * @throws VerifierSdkException if retrieval fails
     */
    @Override
    public ReqE2e getSession(String txId) {
        try {
            log.debug("Getting E2E session for txId: {}", txId);

            // 1. Look up transaction ID
            Long transactionId = transactionManager.getTransactionId(txId);

            // 2. Look up E2E session
            E2e e2e = e2eQueryService.findByTransactionId(transactionId);
            log.debug("Found E2E session for transactionId: {}", transactionId);

            // 3. Convert to ReqE2e and return
            // Note: sessionKey holds the private key, so it must be converted to a public key
            // BaseCryptoUtil.compressPublicKey restores the public key from the private key
            // Uses the existing utility rather than CryptoHelper for legacy compatibility
            byte[] privateKeyBytes = cryptoHelper.decodeMultibase(e2e.getSessionKey());
            byte[] publicKeyBytes = org.omnione.did.base.util.BaseCryptoUtil.compressPublicKey(
                    privateKeyBytes,
                    org.omnione.did.base.datamodel.enums.EccCurveType.fromValue(e2e.getCurve())
            );
            String publicKey = cryptoHelper.encodeMultibase(publicKeyBytes);

            return ReqE2e.builder()
                    .nonce(e2e.getNonce())
                    .curve(e2e.getCurve())
                    .publicKey(publicKey)
                    .cipher(e2e.getCipher())
                    .padding(e2e.getPadding())
                    .build();

        } catch (OpenDidException e) {
            log.error("Failed to get E2E session for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_E2E_SESSION_NOT_FOUND,
                    "Failed to get E2E session: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error getting E2E session for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_E2E_SESSION_NOT_FOUND,
                    "Unexpected error getting E2E session: " + e.getMessage());
        }
    }

    /**
     * Deletes an E2E session.
     * 
     * @param txId Transaction ID
     */
    @Override
    public void removeSession(String txId) {
        try {
            log.debug("Removing E2E session for txId: {}", txId);
            
            // 1. Look up transaction ID
            Long transactionId = transactionManager.getTransactionId(txId);
            
            // 2. Look up and delete E2E session
            E2e e2e = e2eQueryService.findByTransactionId(transactionId);
            e2eRepository.delete(e2e);
            
            log.info("E2E session removed successfully for txId: {}", txId);
            
        } catch (OpenDidException e) {
            log.warn("Failed to remove E2E session for txId: {}", txId, e);
            // Removal failures are not rethrown (the session may already be gone)
        } catch (Exception e) {
            log.warn("Unexpected error removing E2E session for txId: {}", txId, e);
        }
    }

    /**
     * Checks whether an E2E session exists.
     * 
     * @param txId Transaction ID
     * @return true if the session exists, false otherwise
     */
    @Override
    public boolean existsSession(String txId) {
        try {
            log.debug("Checking E2E session existence for txId: {}", txId);
            
            // 1. Look up transaction ID
            Long transactionId = transactionManager.getTransactionId(txId);
            
            // 2. Check whether the E2E session exists
            boolean exists = e2eRepository.findByTransactionId(transactionId).isPresent();
            log.debug("E2E session exists for txId {}: {}", txId, exists);
            
            return exists;
            
        } catch (Exception e) {
            log.debug("E2E session does not exist for txId: {}", txId);
            return false;
        }
    }

    /**
     * Decrypts encrypted data (ECDH-based E2E decryption).
     *
     * E2E decryption protocol:
     * 1. Extract the holder public key (Multibase decoding)
     * 2. ECDH: verifier private key + holder public key -> shared secret
     * 3. Session key = KDF(shared secret + nonce)
     * 4. Plaintext VP = Decrypt(encVp, session key, iv)
     *
     * Note: encHolderPublicKey is not encrypted; it is the public key encoded in Multibase only.
     *
     * @param txId Transaction ID
     * @param encHolderPublicKey holder public key (Multibase-encoded)
     * @param encVp encrypted VP (Multibase-encoded)
     * @param iv IV (Multibase-encoded)
     * @return decrypted VP (JSON string)
     * @throws VerifierSdkException if decryption fails
     */
    @Override
    public String decrypt(String txId, String encHolderPublicKey, String encVp, String iv) {
        try {
            log.debug("Decrypting E2E VP for txId: {}", txId);

            // 1. Look up E2E session
            Long transactionId = transactionManager.getTransactionId(txId);
            E2e e2e = e2eQueryService.findByTransactionId(transactionId);

            // 2. Extract encryption settings (using CryptoHelper)
            byte[] verifierPrivateKeyBytes = cryptoHelper.decodeMultibase(e2e.getSessionKey());
            byte[] nonceBytes = cryptoHelper.decodeMultibase(e2e.getNonce());
            byte[] ivBytes = cryptoHelper.decodeMultibase(iv);

            // 3. Extract holder public key
            // In the ECDH protocol the public key is sent as is (not encrypted)
            // AccE2e.publicKey is the holder's public key encoded in Multibase
            byte[] holderPublicKeyBytes = cryptoHelper.decodeMultibase(encHolderPublicKey);
            log.debug("Extracted Holder public key: {}", Hex.toHexString(holderPublicKeyBytes));

            // 4. Generate ECDH shared secret (using CryptoHelper)
            byte[] sharedSecret = cryptoHelper.generateSharedSecret(
                    holderPublicKeyBytes,
                    verifierPrivateKeyBytes,
                    e2e.getCurve()
            );
            log.debug("Generated ECDH shared secret : {}",  Hex.toHexString(sharedSecret));

            // 5. Derive session key via KDF (using CryptoHelper)
            byte[] sessionKey = cryptoHelper.deriveSessionKey(
                    sharedSecret,
                    nonceBytes,
                    e2e.getCipher()
            );
            log.debug("Generated session key from shared secret and nonce : {}", Hex.toHexString(sessionKey));

            log.debug("ivBytes: {}", Hex.toHexString(ivBytes));

            // 6. Decrypt VP (using CryptoHelper)
            byte[] encVpBytes = cryptoHelper.decodeMultibase(encVp);
            byte[] decryptedVpBytes = cryptoHelper.decrypt(
                    encVpBytes,
                    sessionKey,
                    ivBytes,
                    e2e.getCipher(),
                    e2e.getPadding()
            );

            String vpJson = new String(decryptedVpBytes, StandardCharsets.UTF_8);
            log.info("E2E VP decrypted successfully for txId: {}", txId);

            return vpJson;

        } catch (OpenDidException e) {
            log.error("Failed to decrypt E2E VP for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_DECRYPTION_FAILED,
                    "Failed to decrypt E2E VP: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error decrypting E2E VP for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_DECRYPTION_FAILED,
                    "Unexpected error decrypting E2E VP: " + e.getMessage());
        }
    }
}
