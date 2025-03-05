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
import org.omnione.did.base.db.constant.VerifierStatus;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.verifier.v1.admin.dto.GetVerifierInfoReqDto;
import org.omnione.did.verifier.v1.agent.service.StorageService;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Service
public class VerifierManagementService {
    private final VerifierInfoQueryService verifierInfoQueryService;
    private final StorageService storageService;

    public GetVerifierInfoReqDto getVerifierInfo() {
        VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();

        if (verifierInfo.getStatus() != VerifierStatus.ACTIVATE) {
            return GetVerifierInfoReqDto.fromEntity(verifierInfo);
        }

        DidDocument didDocument = storageService.findDidDoc(verifierInfo.getDid());
        return GetVerifierInfoReqDto.fromEntity(verifierInfo, didDocument);
    }
}
