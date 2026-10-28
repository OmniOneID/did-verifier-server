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
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.property.VerifierProperty;
import org.omnione.did.data.model.provider.ProviderDetail;
import org.omnione.did.verifier.v1.admin.service.VerifierInfoQueryService;
import org.omnione.did.verifier.v1.agent.helper.PublishCertificateHelper;
import org.omnione.did.verifier.v1.provider.VerifierInfoProvider;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.omnione.did.verifier.v1.common.service.StorageService;
import org.springframework.stereotype.Component;

/**
 * VerifierInfoProvider implementation (adapter pattern).
 *
 * Purpose:
 * - Provides the verifier information stored as admin settings (DB) through the SDK interface
 * - Retrieves the verifier DID Document through StorageService
 *
 * Design principles:
 * - Returns the core library type (ProviderDetail) directly
 * - The adapter only performs conversion (no business logic)
 *
 * Dependencies:
 * - VerifierInfoQueryService: reads the admin settings stored in the DB (verifier table)
 * - VerifierProperty: verifier.* settings in application.yml (used for DID Document lookup)
 * - StorageService: used for DID Document lookup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VerifierInfoProviderImpl implements VerifierInfoProvider {

    private final VerifierInfoQueryService verifierInfoQueryService;
    private final VerifierProperty verifierProperty;
    private final StorageService storageService;

    /**
     * Retrieves the verifier's basic information.
     *
     * Built from the admin settings (verifier table in the DB).
     * The mapping rules are the same as those used for the admin console display (PolicyProfileService).
     *
     * @return ProviderDetail (core type: DID, name, certVcRef, reference URL, etc.)
     * @throws VerifierSdkException if the verifier settings are missing or incomplete
     */
    @Override
    public ProviderDetail getVerifierInfo() {
        log.debug("=== Starting VerifierInfoProviderAdapter.getVerifierInfo ===");

        try {
            // Read the admin settings from the DB (application-verifier.yml is not used)
            VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();

            String did = verifierInfo.getDid();
            String name = verifierInfo.getName();

            if (did == null || did.isBlank()) {
                log.error("Verifier DID is not configured.");
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_VERIFIER_INFO_NOT_FOUND,
                        "Verifier DID is not configured (DB)");
            }

            if (name == null || name.isBlank()) {
                log.warn("Verifier name is not configured. Using the DID as the name.");
            }

            // Build the core ProviderDetail (same mapping as the admin console; see PolicyProfileService)
            ProviderDetail providerDetail = new ProviderDetail();
            providerDetail.setDid(did);
            providerDetail.setName(name != null && !name.isBlank() ? name : did);
            providerDetail.setCertVcRef(PublishCertificateHelper.getCertificateVcURL(verifierInfo));
            providerDetail.setRef(verifierInfo.getServerUrl());

            log.debug("Retrieved verifier info from DB: DID={}, Name={}", did, name);
            return providerDetail;

        } catch (VerifierSdkException e) {
            log.error("Configuration error while retrieving verifier info", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while retrieving verifier info", e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_CONFIGURATION_ERROR,
                    "Failed to get Verifier info: " + e.getMessage());
        }
    }

    /**
     * Retrieves the verifier DID Document.
     *
     * @return DID Document (JSON string)
     * @throws VerifierSdkException if the lookup fails
     */
    @Override
    public String getVerifierDidDocument() {
        log.debug("=== Starting VerifierInfoProviderAdapter.getVerifierDidDocument ===");

        try {
            String verifierDid = verifierProperty.getDid();

            if (verifierDid == null || verifierDid.isBlank()) {
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_VERIFIER_INFO_NOT_FOUND,
                        "Verifier DID is not configured");
            }

            org.omnione.did.data.model.did.DidDocument didDoc = storageService.findDidDoc(verifierDid);
            String didDocument = didDoc != null ? didDoc.toJson() : null;

            log.debug("Retrieved verifier DID Document: DID={}", verifierDid);
            return didDocument;

        } catch (VerifierSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while retrieving verifier DID Document", e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_DID_DOCUMENT_NOT_FOUND,
                    "Failed to get Verifier DID Document: " + e.getMessage());
        }
    }

    /**
     * Retrieves the verifier DID.
     *
     * @return Verifier DID
     * @throws VerifierSdkException if the DID is not configured
     */
    @Override
    public String getVerifierDid() {
        String did = verifierProperty.getDid();

        if (did == null || did.isBlank()) {
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_VERIFIER_INFO_NOT_FOUND,
                    "Verifier DID is not configured in application.yml");
        }

        return did;
    }
}
