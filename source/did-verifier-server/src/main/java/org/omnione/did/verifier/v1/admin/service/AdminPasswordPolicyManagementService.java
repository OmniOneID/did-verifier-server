/*
 * Copyright 2025 OmniOne.
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
package org.omnione.did.verifier.v1.admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.db.domain.AdminPasswordPolicy;
import org.omnione.did.base.db.repository.AdminPasswordPolicyRepository;
import org.omnione.did.verifier.v1.admin.dto.AdminPasswordPolicyDto;
import org.omnione.did.verifier.v1.admin.dto.RegisterAdminPasswordPolicyReqDto;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminPasswordPolicyManagementService {
    private final AdminPasswordPolicyRepository adminPasswordPolicyRepository;

    public AdminPasswordPolicyDto getAdminPasswordPolicy() {
        return adminPasswordPolicyRepository.findTop1ByOrderByIdAsc()
                .map(AdminPasswordPolicyDto::fromAdminPasswordPolicy)
                .orElse(null);
    }

    public AdminPasswordPolicyDto registerAdminPasswordPolicy(RegisterAdminPasswordPolicyReqDto reqDto) {
        AdminPasswordPolicy policy = adminPasswordPolicyRepository.findTop1ByOrderByIdAsc()
                .orElse(AdminPasswordPolicy.builder().build());

        policy.setMinLength(reqDto.getMinLength());
        policy.setRequireUppercase(reqDto.getRequireUppercase());
        policy.setRequireNumber(reqDto.getRequireNumber());
        policy.setRequireSpecial(reqDto.getRequireSpecial());
        policy.setPasswordExpiryDays(reqDto.getPasswordExpiryDays());

        return AdminPasswordPolicyDto.fromAdminPasswordPolicy(adminPasswordPolicyRepository.save(policy));
    }
}
