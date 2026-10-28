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

package org.omnione.did.verifier.v1.protocol.service.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StatusClaimParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<StatusListRef> parse(String sdJwt) {
        try {
            String issuerJwt = sdJwt.split("~", 2)[0];
            JsonNode payload = JwtPayloadUtils.parseJwtPayload(objectMapper, issuerJwt);

            JsonNode statusList = payload.path("status").path("status_list");
            if (statusList.isMissingNode()) return Optional.empty();

            JsonNode idxNode = statusList.get("idx");
            JsonNode uriNode = statusList.get("uri");
            if (idxNode == null || uriNode == null) return Optional.empty();

            return Optional.of(new StatusListRef(idxNode.asInt(), uriNode.asText()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
