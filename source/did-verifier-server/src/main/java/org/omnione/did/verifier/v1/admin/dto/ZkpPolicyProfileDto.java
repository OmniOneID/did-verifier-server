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
import org.omnione.did.base.datamodel.enums.ProfileType;
import org.omnione.did.base.db.domain.ZkpPolicyProfile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZkpPolicyProfileDto {
    private Long id;
    private String profileId;
    private ProfileType type;
    private String title;
    private String description;
    private String encoding;
    private String language;
    private Long zkpProofRequestId;
    private String zkpProofRequestName;
    private String createdAt;
    private String updatedAt;
    private Long policyCount;

    public static ZkpPolicyProfileDto fromDomain(ZkpPolicyProfile zkpPolicyProfile) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ZkpPolicyProfileDto.builder()
                .id(zkpPolicyProfile.getId())
                .profileId(zkpPolicyProfile.getProfileId())
                .type(zkpPolicyProfile.getType())
                .title(zkpPolicyProfile.getTitle())
                .description(zkpPolicyProfile.getDescription())
                .encoding(zkpPolicyProfile.getEncoding())
                .language(zkpPolicyProfile.getLanguage())
                .zkpProofRequestId(zkpPolicyProfile.getZkpProofRequestId())
                .createdAt(formatInstant(zkpPolicyProfile.getCreatedAt(), formatter))
                .updatedAt(formatInstant(zkpPolicyProfile.getUpdatedAt(), formatter))
                .build();
    }

    public static ZkpPolicyProfileDto fromDomain(ZkpPolicyProfile zkpPolicyProfile, long policyCount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ZkpPolicyProfileDto.builder()
                .id(zkpPolicyProfile.getId())
                .profileId(zkpPolicyProfile.getProfileId())
                .type(zkpPolicyProfile.getType())
                .title(zkpPolicyProfile.getTitle())
                .description(zkpPolicyProfile.getDescription())
                .encoding(zkpPolicyProfile.getEncoding())
                .language(zkpPolicyProfile.getLanguage())
                .zkpProofRequestId(zkpPolicyProfile.getZkpProofRequestId())
                .createdAt(formatInstant(zkpPolicyProfile.getCreatedAt(), formatter))
                .updatedAt(formatInstant(zkpPolicyProfile.getUpdatedAt(), formatter))
                .policyCount(policyCount)
                .build();
    }

    private static String formatInstant(Instant instant, DateTimeFormatter formatter) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }


}
