package org.omnione.did.verifier.v1.common.service;

import lombok.extern.slf4j.Slf4j;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.vc.VcMeta;
import org.omnione.did.zkp.datamodel.definition.CredentialDefinition;
import org.omnione.did.zkp.datamodel.schema.CredentialSchema;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("sample")
public class MockStorageServiceImpl implements StorageService {

    @Override
    public DidDocument findDidDoc(String didKeyUrl) {
        return null;
    }

    @Override
    public CredentialSchema getZKPCredential(String credentialSchemaId) {
        return null;
    }

    @Override
    public CredentialDefinition getZKPCredentialDefinition(String credentialDefinitionId) {
        return null;
    }

    @Override
    public VcMeta getVcMeta(String vcId) {
        return null;
    }
}
