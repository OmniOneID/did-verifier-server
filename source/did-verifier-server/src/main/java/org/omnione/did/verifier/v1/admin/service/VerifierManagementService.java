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
import org.omnione.did.base.db.constant.VerifierStatus;
import org.omnione.did.base.db.domain.CertificateVc;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.util.BaseMultibaseUtil;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.verifier.v1.admin.dto.GetVerifierInfoReqDto;
import org.omnione.did.verifier.v1.admin.dto.SendCertificateVcReqDto;
import org.omnione.did.verifier.v1.admin.dto.SendEntityInfoReqDto;
import org.omnione.did.verifier.v1.agent.service.CertificateVcQueryService;
import org.omnione.did.verifier.v1.common.dto.EmptyResDto;
import org.omnione.did.verifier.v1.common.service.StorageService;
import org.springframework.stereotype.Service;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class VerifierManagementService {
    private final VerifierInfoQueryService verifierInfoQueryService;
    private final StorageService storageService;
    private final CertificateVcQueryService certificateVcQueryService;

    public GetVerifierInfoReqDto getVerifierInfo() {
        VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();

        if (verifierInfo.getStatus() != VerifierStatus.ACTIVATE) {
            return GetVerifierInfoReqDto.fromEntity(verifierInfo);
        }

        DidDocument didDocument = storageService.findDidDoc(verifierInfo.getDid());
        return GetVerifierInfoReqDto.fromEntity(verifierInfo, didDocument);
    }

    public EmptyResDto createCertificateVc(SendCertificateVcReqDto sendCertificateVcReqDto) {
        byte[] decodedVc = BaseMultibaseUtil.decode(sendCertificateVcReqDto.getCertificateVc());
        log.debug("Decoded VC: {}", new String(decodedVc));

        certificateVcQueryService.save(CertificateVc.builder()
                .vc(new String(decodedVc))
                .build());

        return new EmptyResDto();
    }

    public EmptyResDto updateEntityInfo(SendEntityInfoReqDto sendEntityInfoReqDto) {
        VerifierInfo existedVerifier = verifierInfoQueryService.getVerifierInfoOrNull();

        if (existedVerifier == null) {
            verifierInfoQueryService.save(VerifierInfo.builder()
                    .name(sendEntityInfoReqDto.getName())
                    .did(sendEntityInfoReqDto.getDid())
                    .status(VerifierStatus.ACTIVATE)
                    .serverUrl(sendEntityInfoReqDto.getServerUrl())
                    .certificateUrl(sendEntityInfoReqDto.getCertificateUrl())
                    .build());
        } else {
            existedVerifier.setName(sendEntityInfoReqDto.getName());
            existedVerifier.setDid(sendEntityInfoReqDto.getDid());
            existedVerifier.setServerUrl(sendEntityInfoReqDto.getServerUrl());
            existedVerifier.setCertificateUrl(sendEntityInfoReqDto.getCertificateUrl());
            existedVerifier.setStatus(VerifierStatus.ACTIVATE);
            verifierInfoQueryService.save(existedVerifier);
        }

        return new EmptyResDto();
    }
}
