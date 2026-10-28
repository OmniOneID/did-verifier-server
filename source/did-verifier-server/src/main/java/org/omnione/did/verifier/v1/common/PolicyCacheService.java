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

package org.omnione.did.verifier.v1.common;

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.Policy;
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Caching service for policy lookups.
 * Prevents repeated database queries for the same policyId during the verification flow.
 */
@Service
@RequiredArgsConstructor
public class PolicyCacheService {

    private final PolicyRepository policyRepository;

    @Cacheable(value = "policy", key = "#policyId")
    public Policy findByPolicyId(String policyId) {
        return policyRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.VP_POLICY_NOT_FOUND));
    }

    @Cacheable(value = "policy", key = "#policyId")
    public Optional<Policy> findOptionalByPolicyId(String policyId) {
        return policyRepository.findByPolicyId(policyId);
    }

    @CacheEvict(value = {"policy", "verificationPolicy"}, allEntries = true)
    public void evictAll() {
        // Cache eviction only
    }
}
