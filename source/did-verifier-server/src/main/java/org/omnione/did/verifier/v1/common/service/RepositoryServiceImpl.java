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

package org.omnione.did.verifier.v1.common.service;

import com.google.gson.JsonSyntaxException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseCoreDidUtil;

import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.common.util.DidUtil;
import org.omnione.did.core.manager.DidManager;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.enums.vc.VcStatus;
import org.omnione.did.data.model.vc.VcMeta;
import org.omnione.did.verifier.v1.agent.api.RepositoryFeign;
import org.omnione.did.verifier.v1.agent.api.dto.DidDocApiResDto;
import org.omnione.did.zkp.datamodel.definition.CredentialDefinition;
import org.omnione.did.zkp.datamodel.schema.CredentialSchema;
import org.omnione.did.zkp.datamodel.util.GsonWrapper;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Storage service implementation for managing DID documents and verifiable credentials.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Primary
@Profile("lss")
public class RepositoryServiceImpl implements StorageService {
    private final RepositoryFeign repositoryFeign;

    /**
     * Finds a DID document by DID key URL.
     *
     * @param didKeyUrl The DID key URL of the document to find
     * @return The found DID document
     * @throws OpenDidException If the DID document cannot be found
     */
    @Override
    public DidDocument findDidDoc(String didKeyUrl) {
        try {
            String didDocument = repositoryFeign.getDid(didKeyUrl);

            DidManager didManager = BaseCoreDidUtil.parseDidDoc(didDocument);

            return didManager.getDocument();
        } catch (OpenDidException e) {
            log.error("Failed to find DID document.", e);
            throw e;
        } catch (FeignException e) {
            log.error("Failed to find DID document.", e);
            throw new OpenDidException(ErrorCode.FAILED_TO_FIND_DID_DOC);
        }
    }


    @Override
    public CredentialSchema getZKPCredential(String credentialSchemaId) {
        String credentialSchemaJson = repositoryFeign.getCredentialSchema(credentialSchemaId);
        return parseCredentialSchema(credentialSchemaJson);
    }

    private CredentialSchema parseCredentialSchema(String credentialSchemaJson) {
        try {
            log.debug("\t--> Parsing Credential Schema JSON");

            return GsonWrapper.getGson().fromJson(credentialSchemaJson, CredentialSchema.class);
        } catch (JsonSyntaxException e) {
            log.error("\t--> Failed to decode or parse Credential Schema: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }
    }

    @Override
    public CredentialDefinition getZKPCredentialDefinition(String credentialDefinitionId) {
        String credentialDefinitionJson = repositoryFeign.getCredentialDefinition(credentialDefinitionId);
        return parseCredentialDefinition(credentialDefinitionJson);
    }

    @Override
    public VcMeta getVcMeta(String vcId) {
        String vcMetaData = repositoryFeign.getVcMetaData(vcId);
        return GsonWrapper.getGson().fromJson(vcMetaData, VcMeta.class);
    }

    private CredentialDefinition parseCredentialDefinition(String credentialDefinitionJson) {
        try {
            log.debug("\t--> Parsing Credential Definition");
            return GsonWrapper.getGson().fromJson(credentialDefinitionJson, CredentialDefinition.class);
        } catch (JsonSyntaxException e) {
            log.error("\t--> Failed to parse Credential Definition: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }
    }
}
