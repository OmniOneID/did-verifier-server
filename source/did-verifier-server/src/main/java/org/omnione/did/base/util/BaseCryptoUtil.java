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

package org.omnione.did.base.util;


import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;

import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.common.util.DateTimeUtil;
import org.omnione.did.crypto.engines.CipherInfo;


import org.omnione.did.crypto.exception.CryptoException;
import org.omnione.did.crypto.keypair.KeyPairInterface;
import org.omnione.did.crypto.util.CryptoUtils;
import org.omnione.did.crypto.util.DigestUtils;
import org.omnione.did.crypto.util.SignatureUtils;
import org.omnione.did.data.model.did.Proof;
import org.omnione.did.data.model.enums.did.ProofPurpose;
import org.omnione.did.data.model.enums.did.ProofType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * The BaseCryptoUtil class provides utility methods for cryptographic operations such as key pair generation, nonce generation, shared secret generation, encryption, and decryption.
 * It is designed to facilitate secure communication and data protection in various applications, ensuring compatibility and ease of use across different systems and protocols.
 *
 */
@Slf4j
public class BaseCryptoUtil {

    /**
     * Generate a key pair.
     *
     * @param eccCurveType The ECC curve type to use for key pair generation.
     * @return The generated key pair.
     * @throws OpenDidException if the key pair generation fails.
     */
    public static KeyPairInterface generateKeyPair(EccCurveType eccCurveType) {
        try {
            return CryptoUtils.generateKeyPair(eccCurveType.toOmnioneDidKeyType());
        } catch (CryptoException e) {
            log.error("Failed to generate key pair: " + e.getMessage());
            throw new OpenDidException(ErrorCode.CRYPTO_KEY_PAIR_GENERATION_FAILED);
        }
    }

    /**
     * Generate a nonce.
     *
     * @param length The length of the nonce to generate.
     * @return The generated nonce.
     * @throws OpenDidException if the nonce generation fails.
     */
    public static byte[] generateNonce(int length) {
        try {
            return CryptoUtils.generateNonce(length);
        } catch (CryptoException e) {
            log.error("Failed to generate nonce: " + e.getMessage());
            throw new OpenDidException(ErrorCode.GENERATE_NONCE_FAILED);
        }
    }


    /**
     * Generate a shared secret.
     * The merged result is hashed using SHA-256 and the length of the result is determined by the symmetric cipher type.
     *
     * @param compressedPublicKey The compressed public key to use for shared secret generation.
     * @param privateKey The private key to use for shared secret generation.
     * @param curveType The ECC curve type to use for shared secret generation.
     * @return The generated shared secret.
     * @throws OpenDidException if the shared secret generation fails.
     */
    public static byte[] generateSharedSecret(byte[] compressedPublicKey, byte[] privateKey, EccCurveType curveType) {
        try {
            return CryptoUtils.generateSharedSecret(compressedPublicKey, privateKey, curveType.toOmnioneEccCurveType());
        } catch (CryptoException e) {
            log.error("Failed to generate shared secret: " + e.getMessage());
            throw new OpenDidException(ErrorCode.CRYPTO_SHARED_SECRET_GENERATION_FAILED);
        }
    }

    /**
     * Merge a shared secret and nonce.
     * The merged result is hashed using SHA-256 and the length of the result is determined by the symmetric cipher type.
     *
     * @param sharedSecret The shared secret to merge.
     * @param nonce The nonce to merge.
     * @param symmetricCipherType The symmetric cipher type to use for merging.
     * @return The merged result.
     * @throws OpenDidException if the shared secret and nonce merge fails.
     */
    public static byte[] mergeSharedSecretAndNonce(byte[] sharedSecret, byte[] nonce, SymmetricCipherType symmetricCipherType) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(sharedSecret, 0, sharedSecret.length);
            digest.update(nonce, 0, nonce.length);

            byte[] combinedResult = digest.digest();

            return switch (symmetricCipherType) {
                case AES_128_CBC, AES_128_ECB -> Arrays.copyOfRange(combinedResult, 0, 16);
                case AES_256_CBC, AES_256_ECB -> Arrays.copyOfRange(combinedResult, 0, 32);
                default -> throw new RuntimeException("Invalid symmetric cipher type: " + symmetricCipherType);
            };
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to merge shared secret and nonce: " + e.getMessage());
            throw new OpenDidException(ErrorCode.CRYPTO_SHARED_SECRET_NONCE_MERGE_FAILED);
        }
    }


    /**
     * Decrypt data.
     * @param encrypteData The data to decrypt.
     * @param key The key to use for decryption.
     * @param iv The initialization vector to use for decryption.
     * @param symmetricCipherType The symmetric cipher type to use for decryption.
     * @param symmetricPaddingType The symmetric padding type to use for decryption.
     * @return The decrypted data.
     * @throws OpenDidException if the decryption fails.
     */
    public static byte[] decrypt(byte[] encrypteData, byte[] key, byte[] iv, SymmetricCipherType symmetricCipherType, SymmetricPaddingType symmetricPaddingType) {
        try {
            CipherInfo cipherInfo = new CipherInfo(symmetricCipherType.toOmnioneSymmetricCipherType(), symmetricPaddingType.toOmnioneSymmetricPaddingType());
            return CryptoUtils.decrypt(encrypteData, cipherInfo, key, iv);
        } catch (CryptoException e) {
            log.error("Failed to decrypt data: " + e.getMessage());
            throw new OpenDidException(ErrorCode.CRYPTO_DECRYPTION_FAILED);
        }
    }
    /**
     * Decrypt data.
     * @param encrypteData The data to decrypt.
     * @param key The key to use for decryption.
     * @param iv The initialization vector to use for decryption.
     * @param symmetricCipherType The symmetric cipher type to use for decryption.
     * @param symmetricPaddingType The symmetric padding type to use for decryption.
     * @return The decrypted data.
     */
    public static byte[] decrypt(String encrypteData, byte[] key, byte[] iv, SymmetricCipherType symmetricCipherType, SymmetricPaddingType symmetricPaddingType) {
         return decrypt(BaseMultibaseUtil.decode(encrypteData), key, iv, symmetricCipherType, symmetricPaddingType);
    }

    /**
     * Verify a signature.
     * The public key is decoded from the multibase encoded public key.
     * The signature is verified using the public key.
     * The signData is hashed using SHA-256 and the length of the result is determined by the symmetric cipher type.
     *
     * @param encodedPublicKey Encoded public key
     * @param encodedSignature Encoded signature
     * @param signData Data to verify
     * @param eccCurveType ECC curve type
     * @throws OpenDidException if signature verification fails
     */
    public static void verifySignature(String encodedPublicKey, String encodedSignature, byte[] signData, EccCurveType eccCurveType) {
        try {
            // Decode the public key
            byte[] publicKeyBytes = BaseMultibaseUtil.decode(encodedPublicKey);
            byte[] signatureBytes = BaseMultibaseUtil.decode(encodedSignature);

            // Verify the signature
            SignatureUtils.verifyCompactSignWithCompressedKey(publicKeyBytes, signData, signatureBytes, eccCurveType.toOmnioneEccCurveType());
        }  catch (CryptoException e) {
            log.error("Failed to verify signature: " + e.getMessage());
            throw new OpenDidException(ErrorCode.SIGNATURE_VERIFICATION_FAILED);
        }
    }


    /**
     * Compress a public key.
     */
    public static byte[] compressPublicKey(byte[] serverPrivateKey, EccCurveType eccCurveType) {
        try {
            return CryptoUtils.compressPublicKey(serverPrivateKey, eccCurveType.toOmnioneEccCurveType());
        } catch (CryptoException e) {
            log.error("Failed to compress public key: " + e.getMessage());
            throw new OpenDidException(ErrorCode.PUBLIC_KEY_COMPRESS_FAILED);
        }
    }

    public static Proof generateProof(ProofType proofType, ProofPurpose proofPurpose, String verificationMethod) {
        Proof proof = new Proof();
        proof.setType(proofType.getRawValue());
        proof.setProofPurpose(proofPurpose.getRawValue());
        proof.setCreated(DateTimeUtil.getCurrentUTCTimeString());
        proof.setVerificationMethod(verificationMethod);

        return proof;
    }

    public static byte[] mergeNonce(byte[] clientNonce, byte[] serverNonce) {
        try {
            return DigestUtils.mergeNonce(clientNonce, serverNonce);
        } catch (CryptoException e) {
            log.error("Failed to merge nonce: " + e.getMessage());
            throw new OpenDidException(ErrorCode.CRYPTO_NONCE_MERGE_FAILED);
        }
    }

    public static byte[] mergeNonce(String encodedClientNonce, String encodedServerNonce) {
        return mergeNonce(BaseMultibaseUtil.decode(encodedClientNonce), BaseMultibaseUtil.decode(encodedServerNonce));
    }
}
