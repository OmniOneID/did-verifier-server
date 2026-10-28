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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.constant.ProtocolType;
import org.omnione.did.verifier.v1.agent.dto.RequestOfferReqDto;
import org.omnione.did.verifier.v1.agent.dto.RequestOfferResDto;
import org.omnione.did.verifier.v1.agent.service.VpOfferApplicationService;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateRequest;
import org.omnione.did.verifier.v1.protocol.api.dto.InitiateResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DidVpProtocolHandler implements ProtocolHandler {

    private final VpOfferApplicationService vpOfferApplicationService;

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.DID_VP;
    }

    @Override
    public InitiateResponse initiate(InitiateRequest request) {
        log.debug("=== DID VP initiate for policyId: {} ===", request.getPolicyId());

        RequestOfferReqDto offerReq = RequestOfferReqDto.builder()
                .policyId(request.getPolicyId())
                .build();

        RequestOfferResDto offerRes = vpOfferApplicationService.requestVpOfferbyQR(offerReq);

        return InitiateResponse.builder()
                .protocol(ProtocolType.DID_VP)
                .sessionId(offerRes.getTxId())
                .payload(offerRes.getPayload())
                .nextEndpoints(Map.of(
                        "requestProfile", "/verifier/api/v1/request-profile",
                        "requestVerify", "/verifier/api/v1/request-verify",
                        "confirmVerify", "/verifier/api/v1/confirm-verify"
                ))
                .build();
    }
}
