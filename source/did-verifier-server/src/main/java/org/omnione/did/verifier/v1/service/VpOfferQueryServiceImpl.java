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
import org.omnione.did.base.db.domain.VpOffer;
import org.omnione.did.base.db.repository.VpOfferRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.springframework.stereotype.Service;

/**
 * Service implementation for querying and managing VpOffer entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VpOfferQueryServiceImpl implements VpOfferQueryService {

    private final VpOfferRepository vpOfferRepository;

    /**
     * Finds a VpOffer by its associated transaction ID.
     *
     * @param transactionId The ID of the transaction to search for.
     * @return The found VpOffer.
     * @throws OpenDidException if no VpOffer is found for the given transaction ID.
     */
    @Override
    public VpOffer findByTransactionId(Long transactionId) {
        return vpOfferRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_OFFER_NOT_FOUND));
    }

    /**
     * Inserts a new VpOffer into the repository.
     *
     * @param vpOffer The VpOffer to be inserted.
     */
    @Override
    public void insertVpOffer(VpOffer vpOffer) {
        vpOfferRepository.save(vpOffer);
    }
}