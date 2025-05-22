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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.omnione.did.base.datamodel.enums.EccCurveType;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;
import org.omnione.did.base.db.domain.ZkpProofRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZkpProofRequestDto {
    private Long id;
    private String name;
    private String version;
    private String requestedAttributes;
    private String requestedPredicates;
    private EccCurveType curve;
    private SymmetricCipherType cipher;
    private SymmetricPaddingType padding;
    private String createdAt;
    private String updatedAt;
    private Long profileCount;

    public static ZkpProofRequestDto fromDomain(ZkpProofRequest zkpProofRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ZkpProofRequestDto.builder()
                .id(zkpProofRequest.getId())
                .name(zkpProofRequest.getName())
                .version(zkpProofRequest.getVersion())
                .requestedAttributes(zkpProofRequest.getRequestedAttributes())
                .requestedPredicates(zkpProofRequest.getRequestedPredicates())
                .curve(zkpProofRequest.getCurve())
                .cipher(zkpProofRequest.getCipher())
                .padding(zkpProofRequest.getPadding())
                .createdAt(formatInstant(zkpProofRequest.getCreatedAt(), formatter))
                .updatedAt(formatInstant(zkpProofRequest.getUpdatedAt(), formatter))
                .build();
    }

    public static ZkpProofRequestDto fromDomain(ZkpProofRequest zkpProofRequest, long profileCount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ZkpProofRequestDto.builder()
                .id(zkpProofRequest.getId())
                .name(zkpProofRequest.getName())
                .version(zkpProofRequest.getVersion())
                .requestedAttributes(zkpProofRequest.getRequestedAttributes())
                .requestedPredicates(zkpProofRequest.getRequestedPredicates())
                .curve(zkpProofRequest.getCurve())
                .cipher(zkpProofRequest.getCipher())
                .padding(zkpProofRequest.getPadding())
                .createdAt(formatInstant(zkpProofRequest.getCreatedAt(), formatter))
                .updatedAt(formatInstant(zkpProofRequest.getUpdatedAt(), formatter))
                .profileCount(profileCount)
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }
}
