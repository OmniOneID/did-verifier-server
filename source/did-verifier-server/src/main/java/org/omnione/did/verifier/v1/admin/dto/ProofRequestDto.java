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
package org.omnione.did.verifier.v1.admin.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;
import org.omnione.did.base.db.domain.ZkpProofRequest;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProofRequestDto {
    private Long id;
    private String name;
    private String version;
    private Map<String, AttributeRequestDto> requestedAttributes;
    private Map<String, PredicateRequestDto> requestedPredicates;
    private EccCurveType curve;
    private SymmetricCipherType cipher;
    private SymmetricPaddingType padding;
    private String createdAt;
    private String updatedAt;

    public static ProofRequestDto fromProofRequest(ZkpProofRequest proofRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, AttributeRequestDto> requestedAttributes = null;
        Map<String, PredicateRequestDto> requestedPredicates = null;

        try {
            requestedAttributes = objectMapper.readValue(proofRequest.getRequestedAttributes(), objectMapper.getTypeFactory().constructMapType(Map.class, String.class, AttributeRequestDto.class));
            requestedPredicates = objectMapper.readValue(proofRequest.getRequestedPredicates(), objectMapper.getTypeFactory().constructMapType(Map.class, String.class, PredicateRequestDto.class));
        } catch (JsonProcessingException e) {
            throw new OpenDidException(ErrorCode.JSON_PARSE_ERROR);
        }

        return ProofRequestDto.builder()
                .id(proofRequest.getId())
                .name(proofRequest.getName())
                .version(proofRequest.getVersion())
                .requestedAttributes(requestedAttributes)
                .requestedPredicates(requestedPredicates)
                .curve(proofRequest.getCurve())
                .cipher(proofRequest.getCipher())
                .padding(proofRequest.getPadding())
                .createdAt(formatInstant(proofRequest.getCreatedAt(), formatter))
                .updatedAt(formatInstant(proofRequest.getUpdatedAt(), formatter))
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }


}
