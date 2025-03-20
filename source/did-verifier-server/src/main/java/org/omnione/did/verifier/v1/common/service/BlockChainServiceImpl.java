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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.ContractApi;
import org.omnione.did.ContractFactory;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.BlockchainProperty;
import org.omnione.did.data.model.did.DidDocAndStatus;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.exception.BlockChainException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Service for managing DID Document operations, including registration and retrieval.
 * This service interacts with the blockchain to register and retrieve DID Documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!repository")
public class BlockChainServiceImpl implements StorageService {

    private ContractApi contractApiInstance = null;

    private final BlockchainProperty blockchainProperty;

    /**
     * Initializes the blockchain connection.
     *
     * @return a ContractApi instance.
     */
    public ContractApi initBlockChain() {
        log.debug("Initializing block chain :: file-path {}", blockchainProperty.getFilePath());
        return ContractFactory.FABRIC.create(blockchainProperty.getFilePath());
    }

    /**
     * Resets the ContractApi instance.
     * Use this method to reinitialize the blockchain connection.
     */
    public ContractApi getContractApiInstance() {
        if (contractApiInstance == null) {
            synchronized (BlockChainServiceImpl.class) {
                if (contractApiInstance == null) {
                    contractApiInstance = initBlockChain();
                }
            }
        }
        return contractApiInstance;
    }


    /**
     * Retrieves a DID document and its status from the blockchain.
     *
     * @param didKeyUrl the DID key URL to search for.
     * @return the DID document and its status.
     * @throws OpenDidException if the DID document cannot be found.
     */
    @Override
    public DidDocument findDidDoc(String didKeyUrl) {

        try {
            ContractApi contractApi = getContractApiInstance();
            DidDocAndStatus didDocAndStatus = (DidDocAndStatus) contractApi.getDidDoc(didKeyUrl);

            return didDocAndStatus.getDocument();
        } catch (BlockChainException e) {
            log.error("Failed to get DID Document: " + e.getMessage());
            throw new OpenDidException(ErrorCode.BLOCKCHAIN_GET_DID_DOC_FAILED);
        } catch (Exception e) {
            log.error("Failed to find DID Document: " + e.getMessage());
            throw new OpenDidException(ErrorCode.DID_DOCUMENT_RETRIEVAL_FAILED);
        }
    }





}
