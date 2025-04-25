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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * VC Schema List Response DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class VcSchemaListResDto {
    private int count;
    private List<VcSchemaItem> vcSchemaList;


    public List<String> getAllCombinedIds() {
        List<String> combinedIds = new ArrayList<>();

        if (vcSchemaList != null) {
            for (VcSchemaItem schema : vcSchemaList) {
                combinedIds.addAll(schema.getCombinedIds());
            }
        }

        return combinedIds;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VcSchemaItem {
        private String schemaId;
        private String issuerDid;
        private String issuerName;
        private String title;
        private String description;
        private VcSchema vcSchema;

        public List<String> getCombinedIds() {
            List<String> combinedIds = new ArrayList<>();

            if (vcSchema != null && vcSchema.credentialSubject != null && vcSchema.credentialSubject.claims != null) {
                for (Claim claim : vcSchema.credentialSubject.claims) {
                    if (claim.namespace != null && claim.items != null) {
                        String namespaceId = claim.namespace.id;
                        for (Item item : claim.items) {
                            if (item.id != null) {
                                combinedIds.add(namespaceId + ":" + item.id);
                            }
                        }
                    }
                }
            }

            return combinedIds;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VcSchema {
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String id;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private String schema;

        private CredentialSubject credentialSubject;
        private String description;
        private Metadata metadata;
        private String title;


        @com.fasterxml.jackson.annotation.JsonProperty("@id")
        public String getId() {
            return id;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("@id")
        public void setId(String id) {
            this.id = id;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("@schema")
        public String getSchema() {
            return schema;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("@schema")
        public void setSchema(String schema) {
            this.schema = schema;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CredentialSubject {
        private List<Claim> claims;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Claim {
        private List<Item> items;
        private Namespace namespace;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String caption;
        private String format;
        private boolean hideValue;
        private String id;
        private String type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Namespace {
        private String id;
        private String name;
        private String ref;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String formatVersion;
        private String language;
    }
}