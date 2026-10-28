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

import java.util.Base64;

final class JwtPayloadUtils {

    private JwtPayloadUtils() {
    }

    static String padBase64Url(String base64url) {
        int padding = (4 - base64url.length() % 4) % 4;
        return base64url + "=".repeat(padding);
    }

    static byte[] decodeBase64Url(String base64url) {
        return Base64.getUrlDecoder().decode(padBase64Url(base64url));
    }

    static JsonNode parseJwtPayload(ObjectMapper objectMapper, String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Not a valid JWT: missing payload segment");
        }
        return objectMapper.readTree(decodeBase64Url(parts[1]));
    }
}
