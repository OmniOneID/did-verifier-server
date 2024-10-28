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

import org.omnione.did.base.db.domain.E2e;

/**
 * Service interface for handling E2E queries related to transactions.
 */
public interface E2EQueryService {
    /**
     * Saves an E2E record.
     *
     * @param e2e E2E record to save.
     */
    void save(E2e e2e);

    /**
     * Finds an E2E record by transaction ID.
     *
     * @param transactionId Transaction ID.
     * @return Found E2E record.
     */
    E2e findByTransactionId(Long transactionId);
}
