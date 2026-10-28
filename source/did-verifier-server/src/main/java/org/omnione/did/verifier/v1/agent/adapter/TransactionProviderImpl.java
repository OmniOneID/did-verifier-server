/*
 * Copyright 2026 OmniOne.
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

import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.Transaction;
import org.omnione.did.verifier.v1.agent.service.TransactionService;
import org.omnione.did.verifier.v1.provider.TransactionProvider;
import org.omnione.did.verifier.v1.exception.VerifierSdkException;
import org.omnione.did.verifier.v1.exception.VerifierSdkErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * SDK TransactionProvider implementation.
 *
 * Implementation notes:
 * - Only supports generating and looking up transaction IDs
 * - Transaction state is managed directly in the application DB
 */
@Slf4j
@Component
public class TransactionProviderImpl implements TransactionProvider {

    private final TransactionService transactionService;

    public TransactionProviderImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    /**
     * Generates a new transaction ID (UUID).
     * 
     * @return Transaction ID
     */
    @Override
    public String createTransactionId() {
        String txId = UUID.randomUUID().toString();
        log.debug("Generated new transaction ID: {}", txId);
        return txId;
    }
    
    /**
     * Looks up the transaction ID (Long).
     *
     * Looks up the DB transaction PK (Long) by txId (UUID string).
     * Required to link other tables such as the E2E session.
     *
     * @param txId Transaction ID (UUID string)
     * @return Transaction PK (Long)
     * @throws VerifierSdkException if the lookup fails
     */
    @Override
    public Long getTransactionId(String txId) {
        if (txId == null || txId.trim().isEmpty()) {
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_FIELD_REQUIRED,
                    "Transaction ID cannot be null or empty");
        }

        try {
            Transaction transaction = transactionService.findTransactionByTxId(txId);
            if (transaction == null) {
                throw new VerifierSdkException(
                        VerifierSdkErrorCode.SDK_TRANSACTION_NOT_FOUND,
                        "Transaction not found: " + txId);
            }

            Long transactionId = transaction.getId();
            log.debug("Found transaction ID {} for txId: {}", transactionId, txId);
            return transactionId;

        } catch (VerifierSdkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get transaction ID for txId: {}", txId, e);
            throw new VerifierSdkException(
                    VerifierSdkErrorCode.SDK_TRANSACTION_NOT_FOUND,
                    "Failed to get transaction ID: " + e.getMessage());
        }
    }
}
