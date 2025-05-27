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
import org.omnione.did.base.db.repository.PolicyRepository;
import org.omnione.did.base.db.repository.projection.PayloadIdProjection;
import org.omnione.did.verifier.v1.admin.dto.PayloadDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PayloadQueryService {
    private final PayloadRepository payloadRepository;
    private final PolicyRepository policyRepository;

    public Page<PayloadDTO> searchPayloadList(String searchKey, String searchValue, Pageable pageable) {
        Page<Payload> payloadPage = payloadRepository.searchPayloadList(searchKey, searchValue, pageable);
        List<Payload> payloadList = payloadPage.getContent();

        // 1. Extract list of payload IDs
        List<String> payloadIds = payloadList.stream()
                .map(Payload::getPayloadId)
                .collect(Collectors.toList());

        // 2. Fetch counts in batch
        List<PayloadIdProjection> countResults = policyRepository.countByPayloadIdIn(payloadIds);

        // 3. Convert results to a Map
        Map<String, Long> countMap = countResults.stream()
                .collect(Collectors.toMap(PayloadIdProjection::getPayloadId, PayloadIdProjection::getCount));

        // 4. Convert to DTOs
        List<PayloadDTO> payloadDtos = payloadList.stream()
                .map(payload -> {
                    Long count = countMap.getOrDefault(payload.getPayloadId(), 0L);
                    PayloadDTO dto = PayloadDTO.fromPayload(payload, count);
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(payloadDtos, pageable, payloadPage.getTotalElements());
    }
}
