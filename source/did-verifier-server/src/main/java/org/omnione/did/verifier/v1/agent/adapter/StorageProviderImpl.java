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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.vc.VcMeta;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.springframework.stereotype.Component;

/**
 * SDK StorageService implementation (adapter pattern).
 * Connects the application's existing StorageService to the SDK interface.
 *
 * Important: DidDocument and VcMeta are GsonWrapper-based, so
 * toJson() must be used instead of Jackson ObjectMapper.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageProviderImpl implements org.omnione.did.verifier.v1.provider.StorageProvider {

    private final org.omnione.did.verifier.v1.common.service.StorageService appStorageService;
    
    /**
     * Retrieves the DID Document for a DID (returned as JSON).
     *
     * @param did DID
     * @return DID Document (JSON string)
     * @throws VerifierSdkException if the DID Document does not exist or the lookup fails
     */
    @Override
    public String findDidDocument(String did) {
        try {
            log.debug("Finding DID Document for DID: {}", did);

            // The application StorageService takes a didKeyUrl,
            // but only the DID is used here, so it is passed as is
            DidDocument didDoc = appStorageService.findDidDoc(did);

            if (didDoc == null) {
                log.warn("DID Document not found: {}", did);
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_DID_DOCUMENT_NOT_FOUND,
                        "DID Document not found: " + did);
            }

            // Convert the DidDocument object to a JSON string (using GsonWrapper)
            // IMPORTANT: use toJson() instead of ObjectMapper (preserves @Expose annotations)
            String json = didDoc.toJson();
            log.debug("DID Document retrieved successfully for DID: {}", did);

            return json;

        } catch (VerifierSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve DID Document for DID: {}", did, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_STORAGE_ERROR,
                    "Failed to retrieve DID Document: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves the VC Meta for a VC ID (returned as JSON).
     *
     * @param vcId VC ID
     * @return VC Meta information (JSON string)
     * @throws VerifierSdkException if the lookup fails
     */
    @Override
    public String getVcMeta(String vcId) {
        try {
            log.debug("Getting VC Meta for VC ID: {}", vcId);

            VcMeta vcMeta = appStorageService.getVcMeta(vcId);

            if (vcMeta == null) {
                log.warn("VC Meta not found: {}", vcId);
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_VC_META_NOT_FOUND,
                        "VC Meta not found: " + vcId);
            }

            // Convert the VcMeta object to a JSON string (using GsonWrapper)
            // IMPORTANT: use toJson() instead of ObjectMapper (preserves @Expose annotations)
            String json = vcMeta.toJson();
            log.debug("VC Meta retrieved successfully for VC ID: {}", vcId);

            return json;

        } catch (VerifierSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve VC Meta for VC ID: {}", vcId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_STORAGE_ERROR,
                    "Failed to retrieve VC Meta: " + e.getMessage());
        }
    }
    
    /**
     * Checks whether a DID Document exists.
     *
     * @param did DID
     * @return true if it exists, false otherwise
     */
    @Override
    public boolean existsDidDocument(String did) {
        try {
            DidDocument didDoc = appStorageService.findDidDoc(did);
            return didDoc != null;
        } catch (Exception e) {
            log.debug("DID Document does not exist or error occurred: {}", did);
            return false;
        }
    }

    // ========================================================================
    // ZKP verification support
    // ========================================================================

    /**
     * Retrieves a ZKP credential schema.
     *
     * @param schemaId Schema ID
     * @return org.omnione.did.zkp.datamodel.schema.CredentialSchema
     * @throws VerifierSdkException if the lookup fails
     */
    @Override
    public org.omnione.did.zkp.datamodel.schema.CredentialSchema getZKPCredential(String schemaId) {
        try {
            log.debug("Retrieving ZKP Credential Schema for schemaId: {}", schemaId);

            // Look up via the application StorageService
            org.omnione.did.zkp.datamodel.schema.CredentialSchema schema =
                appStorageService.getZKPCredential(schemaId);

            if (schema == null) {
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_ZKP_CREDENTIAL_NOT_FOUND,
                        "ZKP Credential Schema not found: " + schemaId);
            }

            return schema;

        } catch (VerifierSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve ZKP Credential Schema for schemaId: {}", schemaId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_STORAGE_ERROR,
                    "Failed to retrieve ZKP Credential Schema: " + e.getMessage());
        }
    }

    /**
     * Retrieves a ZKP credential definition.
     *
     * @param credDefId Credential Definition ID
     * @return org.omnione.did.zkp.datamodel.definition.CredentialDefinition
     * @throws VerifierSdkException if the lookup fails
     */
    @Override
    public org.omnione.did.zkp.datamodel.definition.CredentialDefinition getZKPCredentialDefinition(String credDefId) {
        try {
            log.debug("Retrieving ZKP Credential Definition for credDefId: {}", credDefId);

            // Look up via the application StorageService
            org.omnione.did.zkp.datamodel.definition.CredentialDefinition credDef =
                appStorageService.getZKPCredentialDefinition(credDefId);

            if (credDef == null) {
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_ZKP_CREDENTIAL_DEF_NOT_FOUND,
                        "ZKP Credential Definition not found: " + credDefId);
            }

            return credDef;

        } catch (VerifierSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve ZKP Credential Definition for credDefId: {}", credDefId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_STORAGE_ERROR,
                    "Failed to retrieve ZKP Credential Definition: " + e.getMessage());
        }
    }
}
