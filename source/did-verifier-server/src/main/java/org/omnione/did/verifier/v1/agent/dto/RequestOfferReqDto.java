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

package org.omnione.did.verifier.v1.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.omnione.did.base.datamodel.enums.PresentMode;

/**
 * DTO for VP Offer request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RequestOfferReqDto {
    /**
     * Unique identifier.
     */
    private String id;

    /**
     * policy id.
     */
    @Schema(example = "f1a2b3c4-d5e6-7890-1234-56789abcdef0")
    @NotNull(message = "policy id cannot be null")
    private String policyId;



}
