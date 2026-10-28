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

package org.omnione.did.verifier.v1.protocol.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.oid4vc.dcql.core.credential.CredentialAdapterRegistry;
import org.omnione.did.oid4vc.dcql.core.credential.ParsedCredential;
import org.omnione.did.oid4vc.dcql.exception.DCQLException;
import org.omnione.did.oid4vc.oid4vp.service.OID4VPHelperService;
import org.omnione.did.verifier.v1.protocol.api.dto.ClaimView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Re-parses a stored vp_token into a list of claims for display on the confirm screen.
 * This is for display only, not re-verification, so a failure must not break the status query itself.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Oid4vpClaimExtractionService {

    // See OpenDIDVCCredentialAdapter.extractAllClaimsFromMap(): in the opendid_vc format each claim is a whole
    // {code, caption, type, format, hideValue, value} object (unlike the plain values of other formats).
    private static final String OPENDID_VC_FORMAT = "opendid_vc";

    // In mdoc, claims are grouped one level below the namespace: {namespace: {elementIdentifier: value}}.
    // Left as is, the screen would show a single namespace row with the whole object, so they are flattened per element.
    private static final String MDOC_FORMAT = "mso_mdoc";
    private static final String MDOC_DID_FORMAT = "mso_mdoc-did";

    private final OID4VPHelperService oid4VPHelperService;
    private final ObjectMapper objectMapper;

    public List<ClaimView> extractClaims(String vpTokenJson) {
        if (vpTokenJson == null || vpTokenJson.isBlank()) {
            return List.of();
        }
        List<ClaimView> claims = new ArrayList<>();
        try {
            Map<String, List<Object>> vpTokenMap = oid4VPHelperService.parseVPToken(vpTokenJson);
            for (List<Object> credentials : vpTokenMap.values()) {
                for (Object credentialObj : credentials) {
                    claims.addAll(extractFromCredential(credentialObj));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract claims for display from vp_token", e);
        }
        return claims;
    }

    List<ClaimView> extractFromCredential(Object credentialObj) {
        String raw = toRawString(credentialObj);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        return CredentialAdapterRegistry.getInstance().detectAdapter(raw)
                .map(adapter -> {
                    try {
                        ParsedCredential parsed = adapter.parse(raw);
                        List<ClaimView> result = new ArrayList<>();
                        parsed.getAllClaims().forEach((key, value) ->
                                addClaimViews(result, parsed.getFormat(), key, value));
                        return result;
                    } catch (DCQLException e) {
                        log.warn("Failed to parse credential for claim display: {}", e.getMessage());
                        return List.<ClaimView>of();
                    }
                })
                .orElseGet(() -> {
                    log.debug("No credential adapter detected for claim display");
                    return List.of();
                });
    }

    /** For mdoc, unpacks the namespace map into one row per element; other formats produce a single row as is. */
    private void addClaimViews(List<ClaimView> result, String format, String key, Object value) {
        if (isMdocFormat(format) && value instanceof Map<?, ?> nameSpace) {
            nameSpace.forEach((element, elementValue) ->
                    result.add(new ClaimView(String.valueOf(element), stringifyClaimValue(elementValue))));
            return;
        }
        result.add(toClaimView(format, key, value));
    }

    private boolean isMdocFormat(String format) {
        return MDOC_FORMAT.equals(format) || MDOC_DID_FORMAT.equals(format);
    }

    // For opendid_vc the value is a {caption, value, ...} descriptor object, so caption/value are unpacked from it.
    // Other formats (dc+sd-jwt, etc.) use the value as the claim value as is.
    private ClaimView toClaimView(String format, String key, Object value) {
        if (OPENDID_VC_FORMAT.equals(format) && value instanceof Map<?, ?> claimMap) {
            Object caption = claimMap.get("caption");
            String captionStr = (caption instanceof String cs && !cs.isBlank()) ? cs : key;
            return new ClaimView(captionStr, stringifyClaimValue(claimMap.get("value")));
        }
        return new ClaimView(key, stringifyClaimValue(value));
    }

    private String toRawString(Object credentialObj) {
        if (credentialObj instanceof String s) {
            return s;
        }
        try {
            return objectMapper.writeValueAsString(credentialObj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String stringifyClaimValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }
}
