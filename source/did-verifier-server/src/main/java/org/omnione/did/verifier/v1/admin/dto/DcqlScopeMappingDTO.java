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

package org.omnione.did.verifier.v1.admin.dto;

import lombok.*;
import org.omnione.did.base.db.domain.DcqlScopeMapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DcqlScopeMappingDTO {
    private Long id;
    private String scope;
    private String dcqlQuery;
    private String description;
    private Boolean enabled;
    private String createdAt;
    private String updatedAt;

    public static DcqlScopeMappingDTO toDTO(DcqlScopeMapping entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return DcqlScopeMappingDTO.builder()
                .id(entity.getId())
                .scope(entity.getScope())
                .dcqlQuery(entity.getDcqlQuery())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .createdAt(formatInstant(entity.getCreatedAt(), formatter))
                .updatedAt(formatInstant(entity.getUpdatedAt(), formatter))
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }
}
