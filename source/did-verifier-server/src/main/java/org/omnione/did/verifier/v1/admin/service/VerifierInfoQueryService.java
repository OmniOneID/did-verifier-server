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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.db.repository.VerifierInfoRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.springframework.stereotype.Service;

/**
 * Description...
 */
@RequiredArgsConstructor
@Service
public class VerifierInfoQueryService {

    private final VerifierInfoRepository verifierInfoRepository;

    private static VerifierInfo verifierInfo;

    public VerifierInfo getVerifierInfo() {
        return verifierInfoRepository.findFirstBy().orElseThrow(()
                -> new OpenDidException(ErrorCode.VERIFIER_NOT_FOUND));
    }

    public VerifierInfo getVerifierInfoOrNull() {
        return verifierInfoRepository.findFirstBy().orElse(null);
    }
}
