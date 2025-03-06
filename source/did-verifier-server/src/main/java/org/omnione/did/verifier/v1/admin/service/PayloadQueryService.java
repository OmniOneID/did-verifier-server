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

import lombok.RequiredArgsConstructor;
import org.omnione.did.base.db.domain.Payload;
import org.omnione.did.base.db.repository.PayloadRepository;
import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PayloadQueryService {
    private final PayloadRepository payloadRepository;

    public Page<PayloadDTO> searchPayloadList(String searchKey, String searchValue, Pageable pageable) {
        Page<Payload> payloadPage = payloadRepository.searchPayloadList(searchKey, searchValue, pageable);

        List<PayloadDTO> payloadDtos = payloadPage.getContent().stream()
                .map(PayloadDTO::fromPayload)
                .collect(Collectors.toList());

        return new PageImpl<>(payloadDtos, pageable, payloadPage.getTotalElements());
    }
}
