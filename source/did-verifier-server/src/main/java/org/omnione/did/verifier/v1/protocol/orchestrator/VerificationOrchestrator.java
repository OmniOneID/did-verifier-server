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
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateRequest;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateResponse;
import org.omnione.did.verifier.v1.protocol.handler.ProtocolHandler;
import org.omnione.did.verifier.v1.protocol.registry.ProtocolRegistry;
import org.omnione.did.verifier.v1.protocol.resolver.PolicyProtocolResolver;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationOrchestrator {

    private final PolicyProtocolResolver policyProtocolResolver;
    private final ProtocolRegistry protocolRegistry;

    public InitiateResponse initiate(InitiateRequest request) {
        log.debug("=== VerificationOrchestrator.initiate policyId={} ===", request.getPolicyId());

        try {
            // 1. Resolve the ProtocolType from the policy
            ProtocolType protocolType = policyProtocolResolver.resolve(request.getPolicyId());

            // 2. Look up the handler
            ProtocolHandler handler = protocolRegistry.getHandler(protocolType);

            // 3. Delegate
            return handler.initiate(request);

        } catch (OpenDidException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initiate verification for policyId={}", request.getPolicyId(), e);
            throw new OpenDidException(ErrorCode.FAILED_TO_INITIATE_VERIFICATION);
        }
    }
}
