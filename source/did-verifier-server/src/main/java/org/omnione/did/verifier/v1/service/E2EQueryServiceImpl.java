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
import org.omnione.did.base.db.domain.E2e;
import org.omnione.did.base.db.repository.E2eRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.springframework.stereotype.Service;

/**
 * Handles saving and querying E2E records related to transactions.
 * This class uses the E2eRepository to interact with the database.
 */
@RequiredArgsConstructor
@Service
public class E2EQueryServiceImpl implements E2EQueryService {

    private final E2eRepository e2ERepository;

    /**
     * Saves an E2E record.
     *
     * @param e2E E2E record to save.
     */
    @Override
    public void save(E2e e2E) {
        e2ERepository.save(e2E);
    }

    /**
     * Finds an E2E record by transaction ID.
     *
     * @param transactionId Transaction ID.
     * @return Found E2E record.
     * @throws OpenDidException if the E2E record cannot be found.
     */
    @Override
    public E2e findByTransactionId(Long transactionId) {
        return e2ERepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.E2E_NOT_FOUND));
    }
}
