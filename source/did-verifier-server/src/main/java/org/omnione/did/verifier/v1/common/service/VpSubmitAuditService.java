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

package org.omnione.did.verifier.v1.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.VpSubmit;
import org.omnione.did.base.db.repository.VpSubmitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence service dedicated to VP submission audit records.
 *
 * The VpSubmit record of a submission attempt must be committed even if the caller's
 * transaction rolls back, so that it remains valid as non-repudiation and history evidence.
 * Every save method therefore runs in a separate transaction with REQUIRES_NEW.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VpSubmitAuditService {

    private final VpSubmitRepository vpSubmitRepository;

    /**
     * Records a successful submission. An existing row for the same transactionId is not overwritten.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(Long transactionId, String vp, String holderDid, String format) {
        if (transactionId == null) {
            return;
        }
        try {
            if (vpSubmitRepository.findByTransactionId(transactionId) != null) {
                return;
            }
            vpSubmitRepository.save(VpSubmit.builder()
                    .transactionId(transactionId)
                    .vp(vp)
                    .holderDid(holderDid)
                    .format(format)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to persist VpSubmit success audit for transactionId {}", transactionId, e);
        }
    }

    /**
     * Records a failed submission. vp / holderDid should be passed to the extent they can be recovered
     * at the point of failure, and may be null otherwise.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long transactionId, String vp, String holderDid, String errorCode, String format) {
        if (transactionId == null) {
            return;
        }
        try {
            if (vpSubmitRepository.findByTransactionId(transactionId) != null) {
                return;
            }
            vpSubmitRepository.save(VpSubmit.builder()
                    .transactionId(transactionId)
                    .vp(vp)
                    .holderDid(holderDid)
                    .errorCode(errorCode)
                    .format(format)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to persist VpSubmit failure audit for transactionId {}", transactionId, e);
        }
    }
}
