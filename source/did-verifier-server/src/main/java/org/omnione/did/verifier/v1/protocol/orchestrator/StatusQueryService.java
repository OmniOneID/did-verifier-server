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

package org.omnione.did.verifier.v1.protocol.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.constant.ProtocolType;
import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.constant.TransactionType;
import org.omnione.did.base.db.domain.Transaction;
import org.omnione.did.base.db.domain.VpSubmit;
import org.omnione.did.base.db.repository.VpSubmitRepository;
import org.omnione.did.verifier.v1.agent.service.TransactionService;
import org.omnione.did.verifier.v1.protocol.api.dto.ClaimView;
import org.omnione.did.verifier.v1.protocol.api.dto.StatusResponse;
import org.omnione.did.verifier.v1.protocol.service.Oid4vpClaimExtractionService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusQueryService {

    private final TransactionService transactionService;
    private final VpSubmitRepository vpSubmitRepository;
    private final Oid4vpClaimExtractionService claimExtractionService;

    public StatusResponse getStatus(String sessionId) {
        log.debug("=== StatusQueryService.getStatus sessionId={} ===", sessionId);

        Transaction transaction = transactionService.findTransactionByTxId(sessionId);

        // Expiry check: a PENDING transaction whose expired_at has passed is treated as EXPIRED
        String status = transaction.getStatus().name();
        if (transaction.getStatus() == TransactionStatus.PENDING
                && transaction.getExpired_at().isBefore(Instant.now())) {
            status = "EXPIRED";
        }

        ProtocolType protocol = transaction.getType() == TransactionType.OID4VP
                ? ProtocolType.OID4VP
                : ProtocolType.DID_VP;

        // Only for a completed OID4VP submission, re-parse the claims and attach them for the confirm screen.
        String format = null;
        List<ClaimView> claims = null;
        if (protocol == ProtocolType.OID4VP && "COMPLETED".equals(status)) {
            VpSubmit vpSubmit = vpSubmitRepository.findByTransactionId(transaction.getId());
            if (vpSubmit != null && vpSubmit.getVp() != null) {
                format = vpSubmit.getFormat();
                claims = claimExtractionService.extractClaims(vpSubmit.getVp());
            }
        }

        return StatusResponse.builder()
                .sessionId(sessionId)
                .protocol(protocol)
                .status(status)
                .format(format)
                .claims(claims)
                .build();
    }
}
