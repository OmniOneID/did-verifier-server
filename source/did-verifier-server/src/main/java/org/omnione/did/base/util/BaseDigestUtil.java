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
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.crypto.enums.DigestType;
import org.omnione.did.crypto.exception.CryptoException;
import org.omnione.did.crypto.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for generating hash values using various algorithms.
 * This class provides methods to generate SHA-256 hashes from byte arrays and strings.
 * It also allows generating hashes with specified algorithms.
 *
 */
@Slf4j
public class BaseDigestUtil {
    /**
     * Generate a SHA-256 hash from a string.
     *
     * @param input The input string to hash.
     * @return The SHA-256 hash value as a byte array.
     */
    public static byte[] generateHash(byte[] input) {
        return generateHash(input, DigestType.SHA256);
    }

    /**
     * Generate a SHA-256 hash from a string.
     *
     * @param input The input string to hash.
     * @return The SHA-256 hash value as a byte array.
     */
    public static byte[] generateHash(byte[] input, DigestType digestType)  {
        try {
            return DigestUtils.getDigest(input, digestType);
        } catch (CryptoException e) {
            log.error("Failed to generate hash value.", e);
            throw new OpenDidException(ErrorCode.GENERATE_HASH_FAILED);
        }
    }
}
