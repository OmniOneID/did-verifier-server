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
import org.omnione.did.base.db.domain.VpOffer;

/**
 * Service interface for querying and managing VpOffer entities.
 */
public interface VpOfferQueryService {

    /**
     * Finds a VpOffer by its associated transaction ID.
     *
     * @param transactionId The ID of the transaction to search for.
     * @return The found VpOffer.
     * @throws org.omnione.did.base.exception.OpenDidException if no VpOffer is found for the given transaction ID.
     */
    VpOffer findByTransactionId(Long transactionId);

    /**
     * Inserts a new VpOffer into the repository.
     *
     * @param vpOffer The VpOffer to be inserted.
     */
    void insertVpOffer(VpOffer vpOffer);
}