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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.enums.did.ProofPurpose;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Service for managing and retrieving DID Documents.
 * This class includes a cache mechanism to store DID Documents and a scheduler to periodically refresh the cache.
 *
 * The class uses a ScheduledExecutorService to refresh the cache every hour.
 *
 * Example usage:
 * <pre>
 *     DidDocServiceImpl service = new DidDocServiceImpl();
 *     Object didDoc = service.getDidDocument("did:example:123");
 * </pre>
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DidDocServiceImpl implements DidDocService {

    private final StorageService storageService;
    private final DidDocCache didDocCache = new DidDocCache();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::refreshAllDidDocuments, 0, 1, TimeUnit.HOURS);
    }

    /**
     * Get the DID Document for the given DID.
     * If the document is not in the cache or is outdated, it is fetched from the source (e.g., blockchain).
     *
     * @param did The DID identifier.
     * @return The corresponding DID document.
     * @throws OpenDidException if the DID document cannot be retrieved.
     */
    @Override
    public DidDocument getDidDocument(String did) {
        if (shouldUpdate(did)) {
            updateDidDocument(did);
        }

        DidDocument didDoc = didDocCache.getDidDoc(did);
        if (didDoc == null) {
            throw new OpenDidException(ErrorCode.DID_DOCUMENT_RETRIEVAL_FAILED);
        }

        return didDoc;
    }

    /**
     * Get the verification method for a given proof purpose from the provided DID document.
     * @param didDocument The DID document.
     * @param proofPurpose The proof purpose for which the verification method is required.
     * @return The verification method as a string.
     * @throws OpenDidException if the proof purpose is invalid.
     */
    @Override
    public String getVerificationMethod(DidDocument didDocument, ProofPurpose proofPurpose) {

        String did = didDocument.getId();

        if (ProofPurpose.ASSERTION_METHOD == proofPurpose) {
            return did + "#" + "assert";
        } else if (ProofPurpose.AUTHENTICATION == proofPurpose) {
            return did + "#" + "auth";
        } else if (ProofPurpose.KEY_AGREEMENT == proofPurpose) {
            return did + "#" + "keyagree";
        } else if (ProofPurpose.CAPABILITY_INVOCATION == proofPurpose) {
            return did + "#" + "invoke";
        }

        throw new OpenDidException(ErrorCode.INVALID_PROOF_PURPOSE);
    }
    /**
     * Fetche the DID Document from the source (e.g., blockchain).
     *
     */
    private Object fetchDidDocument(String did) {
        return new Object();
    }
    /**
     * Update the DID Document in the cache for the given DID by fetching the latest version.
     * @param did The DID identifier.
     */
    public void updateDidDocument(String did) {
        DidDocument didDocument = storageService.findDidDoc(did);
        didDocCache.putDidDoc(did, didDocument);
    }

    /**
     * Check if the DID Document for the given DID should be updated.
     * Consider the document outdated if it was last updated more than an hour ago.
     * @param did The DID identifier.
     * @return true if the document should be updated, false otherwise.
     */
    private boolean shouldUpdate(String did) {
        long currentTime = System.currentTimeMillis();
        long lastUpdate = didDocCache.getTimestamp(did);
        return (currentTime - lastUpdate) > TimeUnit.HOURS.toMillis(1);
    }

    /**
     * Refresh all DID Documents in the cache by fetching the latest versions.
     */
    private void refreshAllDidDocuments() {
        for (String did : didDocCache.getAllDids()) {
            updateDidDocument(did);
        }
    }

}
