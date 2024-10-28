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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseBlockChainUtil;
import org.omnione.did.data.model.did.DidDocAndStatus;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.vc.VcMeta;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link StorageService} interface that uses blockchain to manage
 * DID documents and VC metadata.
 *
 * This service interacts with a blockchain-based utility to retrieve and store DID-related data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!repository")
public class BlockChainServiceImpl implements StorageService {

    /**
     * Finds a DID document on the blockchain using the provided DID key URL.
     *
     * @param didKeyUrl URL of the DID key.
     * @return The corresponding DID document.
     * @throws OpenDidException if the DID document cannot be found.
     */
    @Override
    public DidDocument findDidDoc(String didKeyUrl) {
        try {
            DidDocAndStatus didDocAndStatus = BaseBlockChainUtil.findDidDocument(didKeyUrl);
            return didDocAndStatus.getDocument();
        } catch (OpenDidException e) {
            log.error("Failed to find DID Document: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.FAILED_TO_FIND_DID_DOC);
        }
    }

}
