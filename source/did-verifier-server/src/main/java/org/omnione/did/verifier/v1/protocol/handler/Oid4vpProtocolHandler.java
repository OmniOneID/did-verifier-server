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

package org.omnione.did.verifier.v1.protocol.handler;

import com.nimbusds.jose.jwk.ECKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.constant.ProtocolType;
import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.constant.TransactionType;
import org.omnione.did.base.db.domain.Oid4vpSession;
import org.omnione.did.base.db.domain.Oid4vpSessionMapping;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.domain.Transaction;
import org.omnione.did.base.db.repository.Oid4vpSessionJpaRepository;
import org.omnione.did.base.db.repository.Oid4vpSessionMappingRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.oid4vc.oid4vp.dto.ServiceResult;
import org.omnione.did.oid4vc.oid4vp.service.InitiationService;
import org.omnione.did.verifier.v1.admin.service.Oid4vpConfigService;
import org.omnione.did.verifier.v1.agent.service.TransactionService;
import org.omnione.did.verifier.v1.common.PolicyCacheService;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateRequest;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateResponse;
import org.omnione.did.verifier.v1.protocol.security.Oid4vpEncKeyManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Oid4vpProtocolHandler implements ProtocolHandler {

    private final PolicyCacheService policyCacheService;
    private final TransactionService transactionService;
    private final Oid4vpSessionMappingRepository oid4vpSessionMappingRepository;
    private final Oid4vpSessionJpaRepository oid4vpSessionJpaRepository;
    private final InitiationService initiationService;
    private final Oid4vpEncKeyManager encKeyManager;
    private final Oid4vpConfigService oid4vpConfigService;

    private static final String DEFAULT_RESPONSE_MODE = "direct_post.jwt";

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.OID4VP;
    }

    @Override
    @Transactional
    public InitiateResponse initiate(InitiateRequest request) {
        log.debug("=== OID4VP initiate for policyId: {} ===", request.getPolicyId());

        try {
            // 1. Retrieve the policy (cached)
            Policy policy = policyCacheService.findByPolicyId(request.getPolicyId());

            // 2. Check the scope
            String scope = policy.getScope();
            if (scope == null || scope.isBlank()) {
                throw new OpenDidException(ErrorCode.DCQL_SCOPE_MAPPING_NOT_FOUND);
            }

            // 3. Generate a per-transaction ephemeral enc key pair → put its public key into client_metadata
            ECKey ephemeralKeyPair = encKeyManager.generateEphemeralKeyPair();
            String clientMetadataJson = encKeyManager.buildClientMetadataJson(ephemeralKeyPair);

            // 4. Call the SDK InitiationService (direct_post.jwt with the enc jwks)
            // responseMode is read from the "responseMode" key of the admin oid4vp_config (it is validated to be
            // direct_post.jwt when saved — Oid4vpConfigService.validateResponseMode). The admin screen is read-only,
            // so it is effectively a constant, but reading it from the config keeps this the only place to change
            // if other modes are supported.
            Object configuredResponseMode = oid4vpConfigService.getConfig().get("responseMode");
            String responseMode = configuredResponseMode != null
                    ? configuredResponseMode.toString() : DEFAULT_RESPONSE_MODE;

            ServiceResult<Map<String, Object>> result = initiationService.initiateVerification(
                    null,                  // dcqlQuery (null because the request is scope-based)
                    scope,                 // scope
                    responseMode,          // responseMode — requires a JWE-encrypted response
                    clientMetadataJson,    // clientMetadata — enc jwks + encrypted_response_enc_values_supported
                    true                   // useRequestUri
            );

            if (!result.isSuccess()) {
                log.error("SDK initiation failed: {} - {}", result.getErrorCode(), result.getErrorDescription());
                throw new OpenDidException(ErrorCode.OID4VP_INITIATION_FAILED);
            }

            Map<String, Object> sdkResponse = result.getData();

            String oid4vpTransactionId = (String) sdkResponse.get("transaction_id");
            String requestId = (String) sdkResponse.get("request_id");
            String state = (String) sdkResponse.get("state");
            String authorizationRequestUri = (String) sdkResponse.get("authorization_request_uri");

            // 5. Create the verifier transaction
            Transaction transaction = transactionService.insertTransaction(Transaction.builder()
                    .txId(UUID.randomUUID().toString())
                    .type(TransactionType.OID4VP)
                    .status(TransactionStatus.PENDING)
                    .expired_at(transactionService.retrieveTransactionExpiredTime())
                    .build());

            // 6. Save the session mapping (verifier txId ↔ SDK session IDs)
            oid4vpSessionMappingRepository.save(Oid4vpSessionMapping.builder()
                    .txId(transaction.getTxId())
                    .oid4vpTransactionId(oid4vpTransactionId)
                    .oid4vpRequestId(requestId)
                    .state(state)
                    .build());

            // 7. Store the ephemeral private key in the oid4vp_session row created by the SDK (looked up by kid on decryption)
            persistEncKey(oid4vpSessionJpaRepository, encKeyManager, requestId, ephemeralKeyPair);

            log.debug("*** OID4VP initiate completed. txId={}, oid4vpTxId={}, requestId={} ***",
                    transaction.getTxId(), oid4vpTransactionId, requestId);

            return InitiateResponse.builder()
                    .protocol(ProtocolType.OID4VP)
                    .sessionId(transaction.getTxId())
                    .authorizationRequest(authorizationRequestUri)
                    .nextEndpoints(Map.of(
                            "authorizationRequest", "/oid4vp/request/" + requestId,
                            "response", "/oid4vp/response"
                    ))
                    .build();

        } catch (OpenDidException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initiate OID4VP verification", e);
            throw new OpenDidException(ErrorCode.OID4VP_INITIATION_FAILED);
        }
    }

    /** Finds the oid4vp_session row created by the SDK during initiateVerification by requestId and stores the ephemeral private key in it. */
    static void persistEncKey(Oid4vpSessionJpaRepository repository, Oid4vpEncKeyManager encKeyManager,
                               String requestId, ECKey ephemeralKeyPair) {
        Oid4vpSession session = repository.findByRequestId(requestId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.OID4VP_SESSION_MAPPING_NOT_FOUND));
        session.setEncKid(ephemeralKeyPair.getKeyID());
        session.setEncPrivateKeyJwk(encKeyManager.toStorableJwk(ephemeralKeyPair));
        repository.save(session);
    }
}
