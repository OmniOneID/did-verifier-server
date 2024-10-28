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

import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.domain.SubTransaction;
import org.omnione.did.base.db.domain.Transaction;

import java.time.Instant;

/**
 * Transaction service interface for managing transactions and sub-transactions
 */
public interface TransactionService {

    /**
     * Inserts a new transaction into the system.
     *
     * @param transaction Transaction data to insert.
     * @return Inserted transaction.
     */
    Transaction insertTransaction(Transaction transaction);

    /**
     * Finds a transaction by its transaction ID.
     *
     * @param txId Transaction ID to search for.
     * @return Found transaction.
     */
    Transaction findTransactionByTxId(String txId);

    /**
     * Finds a transaction by its offer ID.
     *
     * @param offerId Offer ID to search for.
     * @return Found transaction.
     */
    Transaction findTransactionByOfferId(String offerId);

    /**
     * Saves a sub-transaction in the system.
     *
     * @param subTransaction Sub-transaction data to save.
     * @return Saved sub-transaction.
     */
    SubTransaction saveSubTransaction(SubTransaction subTransaction);

    /**
     * Finds the last sub-transaction for a given transaction ID.
     *
     * @param id Transaction ID.
     * @return Last sub-transaction.
     */
    SubTransaction findLastSubTransaction(Long id);

    /**
     * Retrieves the expiration time of a transaction.
     *
     * @return Transaction expiration time.
     */
    Instant retrieveTransactionExpiredTime();

    /**
     * Updates the status of a transaction.
     *
     * @param id Transaction ID.
     * @param transactionStatus New transaction status.
     */
    void updateTransactionStatus(Long id, TransactionStatus transactionStatus);
}
