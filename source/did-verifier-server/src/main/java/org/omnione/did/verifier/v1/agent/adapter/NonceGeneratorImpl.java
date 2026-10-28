/*
 * Copyright 2026 OmniOne.
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

import org.omnione.did.base.util.BaseCryptoUtil;
import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.crypto.enums.MultiBaseType;
import org.omnione.did.verifier.v1.provider.NonceGenerator;
import org.springframework.stereotype.Component;

/**
 * NonceGenerator implementation.
 * Generates nonces using BaseCryptoUtil.
 */
@Component
public class NonceGeneratorImpl implements NonceGenerator {

    /**
     * Generates a nonce of the given length.
     *
     * @param length nonce length in bytes
     * @return generated nonce (Base64-encoded)
     */
    @Override
    public String generateNonce(int length) {
        byte[] nonceBytes = BaseCryptoUtil.generateNonce(length);
        return BaseMultibaseUtil.encode(nonceBytes, MultiBaseType.base64);
    }

    /**
     * Generates a nonce of the default length (16 bytes).
     *
     * @return generated nonce (Base64-encoded)
     */
    @Override
    public String generateNonce() {
        return generateNonce(16);
    }
}
