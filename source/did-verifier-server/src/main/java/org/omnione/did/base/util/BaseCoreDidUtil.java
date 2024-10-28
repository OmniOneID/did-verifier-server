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

import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.core.manager.DidManager;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.VerificationMethod;

/**
 * Utility class for core DID operations.
 * Provides methods for parsing DID documents and getting verification methods.
 *
 */
public class BaseCoreDidUtil {

    /**
     * Parse DID document
     *
     * @param didDoc DID document
     * @return DID manager
     */
    public static DidManager parseDidDoc(String didDoc) {
        DidManager didManager = new DidManager();
        didManager.parse(didDoc);

        return didManager;
    }

    /**
     * Parse DID document
     *
     * @param didDocument DID document
     * @return DID manager
     */
    public static DidManager parseDidDoc(DidDocument didDocument) {
        DidManager didManager = new DidManager();
        didManager.parse(didDocument.toJson());

        return didManager;
    }


    /**
     * Get verification method from DID document
     *
     * @param didDocument DID document
     * @param keyId Key ID
     * @return Verification method
     */
    public static VerificationMethod getVerificationMethod(DidDocument didDocument, String keyId) {
        DidManager didManager = parseDidDoc(didDocument);
        return didManager.getVerificationMethodByKeyId(keyId);
    }

}
