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

import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.enums.did.ProofPurpose;

/**
 * Service interface for handling DID documents and verification methods.
 *
 * Provides methods for:
 * - Retrieving a DID document by its DID identifier
 * - Obtaining a verification method based on the proof purpose from a DID document
 */
public interface DidDocService {

    /**
     * Retrieves a DID document using the provided DID identifier.
     *
     * @param did The DID identifier.
     * @return The corresponding DID document.
     */
    DidDocument getDidDocument(String did);

    /**
     * Gets the verification method for a given proof purpose from the provided DID document.
     *
     * @param didDocument The DID document.
     * @param proofPurpose The proof purpose for which the verification method is required.
     * @return The verification method as a string.
     */
    String getVerificationMethod(DidDocument didDocument, ProofPurpose proofPurpose);
}
