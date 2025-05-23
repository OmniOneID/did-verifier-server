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
package org.omnione.did.verifier.v1.admin.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListCredentialDefinitionSimpleDto {
    private final Long id;
    private final String credentialDefinitionId;
    private final String credentialDefinitionTag;
    private final String credentialSchemaId;
    private final String issuerDid;
    private final String issuerName;
    private final String name;
    private final String description;
    private final String createdAt;
    private final String updatedAt;
    private final String entityName;

}
