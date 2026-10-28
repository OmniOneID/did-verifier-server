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

package org.omnione.did.verifier.v1.protocol.resolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.constant.ProtocolType;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.verifier.v1.common.PolicyCacheService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyProtocolResolver {

    private final PolicyCacheService policyCacheService;

    public ProtocolType resolve(String policyId) {
        Policy policy = policyCacheService.findByPolicyId(policyId);

        ProtocolType protocolType = policy.getProtocolType();
        log.debug("Resolved policyId={} → protocolType={}", policyId, protocolType);
        return protocolType;
    }
}
