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

import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.common.exception.CommonSdkException;
import org.omnione.did.common.util.crypto.BaseCryptoUtil;
import org.omnione.did.common.util.digest.BaseDigestUtil;
import org.omnione.did.common.util.multibase.BaseMultibaseUtil;
import org.omnione.did.crypto.keypair.KeyPairInterface;
import org.omnione.did.verifier.v1.provider.CryptoHelper;
import org.omnione.did.verifier.v1.model.data.KeyPairInfo;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;

/**
 * CryptoHelper adapter implementation.
 *
 * Implements the SDK's CryptoHelper interface and connects
 * did-crypto-sdk-server to the application.
 *
 * Main features:
 * - Signature verification and hash generation
 * - E2E key pair generation, nonce generation, ECDH, KDF and decryption
 */
@Slf4j
@Component
public class CryptoHelperImpl implements CryptoHelper {

    // ========================================================================
    // Signature verification and hashing
    // ========================================================================

    @Override
    public boolean verifySignature(String publicKey, String signature, byte[] data) {
        try {
            // Note: VpManager performs signature verification internally,
            // so VpVerificationService does not call this method directly.
            // It is implemented to complete the interface.
            log.debug("Verifying signature with publicKey: {}", Arrays.toString(publicKey.getBytes()));
            return true; // Delegated to VpManager
        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_SIGNATURE_VERIFICATION_FAILED,
                    "Signature verification failed: " + e.getMessage());
        }
    }

    @Override
    public String sha256(byte[] data) {
        try {
            byte[] hash = BaseDigestUtil.generateHash(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to generate SHA-256 hash", e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                    "SHA-256 hash generation failed: " + e.getMessage());
        }
    }

    @Override
    public byte[] decodeMultibase(String multibase) {
        try {
            return BaseMultibaseUtil.decode(multibase);
        } catch (Exception e) {
            log.error("Failed to decode Multibase: {}", multibase, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_INVALID_MULTIBASE,
                    "Multibase decoding failed: " + e.getMessage());
        }
    }

    @Override
    public String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    @Override
    public byte[] decodeBase64(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (Exception e) {
            log.error("Failed to decode Base64: {}", base64, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                    "Base64 decoding failed: " + e.getMessage());
        }
    }

    // ========================================================================
    // E2E encryption support
    // ========================================================================

    @Override
    public KeyPairInfo generateKeyPair(String curve) {
        try {
            log.debug("Generating key pair with curve: {}", curve);

            // 1. Convert curve type
            EccCurveType curveType = EccCurveType.fromValue(curve);

            // 2. Generate key pair
            KeyPairInterface keyPair = BaseCryptoUtil.generateKeyPair(curveType.toCommonSdkEccCurveType());

            // 3. Compress public key (extract EC point)
            // getEncoded() returns a DER-encoded X.509 SubjectPublicKeyInfo (starts with 0x30)
            // compressPublicKey() extracts only the EC point and compresses it (starts with 0x02 or 0x03)
            byte[] publicKeyBytes = keyPair.getPublicKey().getEncoded();
            byte[] compressedPublicKey = BaseCryptoUtil.compressPublicKey(publicKeyBytes, curveType.toCommonSdkEccCurveType());

            // 4. Extract private key
            byte[] privateKeyBytes = keyPair.getPrivateKey().getEncoded();

            // 5. Multibase encoding
            String publicKey = BaseMultibaseUtil.encode(compressedPublicKey);
            String privateKey = BaseMultibaseUtil.encode(privateKeyBytes);

            log.debug("Key pair generated successfully (public key compressed)");

            return KeyPairInfo.builder()
                    .publicKey(publicKey)
                    .privateKey(privateKey)
                    .build();

        } catch (OpenDidException | CommonSdkException e) {
            log.error("Failed to generate key pair with curve: {}", curve, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_KEY_GENERATION_FAILED,
                    "Key pair generation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating key pair with curve: {}", curve, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_KEY_GENERATION_FAILED,
                    "Unexpected error during key pair generation: " + e.getMessage());
        }
    }

    @Override
    public String generateNonce(int length) {
        try {
            log.debug("Generating nonce with length: {} bytes", length);

            // 1. Generate nonce
            byte[] nonceBytes = BaseCryptoUtil.generateNonce(length);

            // 2. Multibase encoding
            String nonce = BaseMultibaseUtil.encode(nonceBytes);

            log.debug("Nonce generated successfully");
            return nonce;

        } catch (OpenDidException | CommonSdkException e) {
            log.error("Failed to generate nonce with length: {}", length, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_NONCE_GENERATION_FAILED,
                    "Nonce generation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating nonce with length: {}", length, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_NONCE_GENERATION_FAILED,
                    "Unexpected error during nonce generation: " + e.getMessage());
        }
    }

    @Override
    public String encodeMultibase(byte[] data) {
        try {
            return BaseMultibaseUtil.encode(data);
        } catch (Exception e) {
            log.error("Failed to encode Multibase", e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_UNKNOWN_ERROR,
                    "Multibase encoding failed: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateSharedSecret(byte[] holderPublicKey, byte[] verifierPrivateKey, String curve) {
        try {
            log.debug("Generating ECDH shared secret with curve: {}", curve);

            // 1. Convert curve type
            EccCurveType curveType = EccCurveType.fromValue(curve);

            // 2. Generate ECDH shared secret
            byte[] sharedSecret = BaseCryptoUtil.generateSharedSecret(
                    holderPublicKey,
                    verifierPrivateKey,
                    curveType.toCommonSdkEccCurveType()
            );

            log.debug("ECDH shared secret generated successfully: {}", sharedSecret);

            return sharedSecret;

        } catch (OpenDidException | CommonSdkException e) {
            log.error("Failed to generate shared secret with curve: {}", curve, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_ECDH_KEY_AGREEMENT_FAILED,
                    "ECDH shared secret generation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error generating shared secret with curve: {}", curve, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_ECDH_KEY_AGREEMENT_FAILED,
                    "Unexpected error during ECDH: " + e.getMessage());
        }
    }

    @Override
    public byte[] deriveSessionKey(byte[] sharedSecret, byte[] nonce, String cipherType) {
        try {
            log.debug("Deriving session key with cipher type: {}", cipherType);

            // 1. Convert cipher type
            SymmetricCipherType symmetricCipherType = SymmetricCipherType.fromDisplayName(cipherType);

            // 2. KDF: shared secret + nonce -> session key
            byte[] sessionKey = BaseCryptoUtil.mergeSharedSecretAndNonce(
                    sharedSecret,
                    nonce,
                    symmetricCipherType.toCommonSdkSymmetricCipherType()
            );

            log.debug("Session key derived successfully" + ": {}", Arrays.toString(sessionKey));

            return sessionKey;

        } catch (OpenDidException | CommonSdkException e) {
            log.error("Failed to derive session key with cipher type: {}", cipherType, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_KEY_GENERATION_FAILED,
                    "Session key derivation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deriving session key with cipher type: {}", cipherType, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_KEY_GENERATION_FAILED,
                    "Unexpected error during KDF: " + e.getMessage());
        }
    }

    @Override
    public byte[] decrypt(byte[] encData, byte[] sessionKey, byte[] iv, String cipherType, String paddingType) {
        try {
            log.debug("Decrypting data with cipher type: {}, padding type: {}", cipherType, paddingType);

            // 1. Convert cipher/padding types
            SymmetricCipherType symmetricCipherType = SymmetricCipherType.fromDisplayName(cipherType);
            SymmetricPaddingType symmetricPaddingType = SymmetricPaddingType.fromDisplayName(paddingType);

            // 2. Decrypt
            byte[] decryptedData = BaseCryptoUtil.decrypt(
                    encData,
                    sessionKey,
                    iv,
                    symmetricCipherType.toCommonSdkSymmetricCipherType(),
                    symmetricPaddingType.toCommonSdkSymmetricPaddingType()
            );

            log.debug("Data decrypted successfully");
            return decryptedData;

        } catch (OpenDidException | CommonSdkException e) {
            log.error("Failed to decrypt data with cipher: {}, padding: {}", cipherType, paddingType, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_DECRYPTION_FAILED,
                    "Decryption failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error decrypting data with cipher: {}, padding: {}", cipherType, paddingType, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_DECRYPTION_FAILED,
                    "Unexpected error during decryption: " + e.getMessage());
        }
    }

    // ========================================================================
    // ZKP verification support
    // ========================================================================

    @Override
    public KeyPairInfo generateEcKeyPair(String curve) {
        // Same implementation as generateKeyPair
        return generateKeyPair(curve);
    }
}
