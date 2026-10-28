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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.property.VerifierProperty;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.impl.MDocVPVerifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialStatusChecker {

    private static final MDocVPVerifier MDOC_VERIFIER = new MDocVPVerifier();

    private final StatusClaimParser parser;
    private final MdocStatusClaimParser mdocParser;
    private final StatusListTokenFetcher fetcher;
    private final StatusListTokenVerifier tokenVerifier;
    private final StatusListBitDecoder decoder;
    private final VerifierProperty verifierProperty;

    public void checkAll(Map<String, List<Object>> vpTokenMap) {
        if (vpTokenMap == null) return;
        for (List<Object> credentials : vpTokenMap.values()) {
            if (credentials == null) continue;
            for (Object credential : credentials) {
                if (!(credential instanceof String value)) continue;
                if (value.contains("~")) {
                    check(parser.parse(value), "SD-JWT");
                } else if (MDOC_VERIFIER.supports(value)) {
                    // mso_mdoc: the status reference lives inside the MSO (CBOR), but the lookup and evaluation are the same as for SD-JWT.
                    check(mdocParser.parse(value), "mso_mdoc");
                }
            }
        }
    }

    private void check(Optional<StatusListRef> refOpt, String format) {
        if (refOpt.isEmpty()) {
            log.debug("No status claim in {}, skipping status check", format);
            return;
        }
        StatusListRef ref = refOpt.get();
        log.debug("Checking credential status: idx={}, uri={}", ref.idx(), ref.uri());

        String tokenJwt;
        try {
            tokenJwt = fetcher.fetch(ref.uri());
        } catch (RuntimeException e) {
            if (verifierProperty.getStatusList().isFailOnFetchError()) {
                if (e instanceof OpenDidException) throw (OpenDidException) e;
                throw new OpenDidException(ErrorCode.STATUS_LIST_FETCH_FAILED);
            }
            log.warn("Failed to fetch status list (FAIL-OPEN): {}", e.getMessage());
            return;
        }

        StatusListTokenPayload tokenPayload = tokenVerifier.verify(tokenJwt, ref.uri());

        try {
            int status = decoder.extract(tokenPayload.lst(), tokenPayload.bits(), ref.idx());
            switch (status) {
                case 0 -> log.debug("Credential status VALID at idx={}", ref.idx());
                case 1 -> {
                    log.warn("Credential INVALID at idx={}, uri={}", ref.idx(), ref.uri());
                    throw new OpenDidException(ErrorCode.STATUS_LIST_CREDENTIAL_INVALID);
                }
                case 2 -> {
                    log.warn("Credential SUSPENDED at idx={}, uri={}", ref.idx(), ref.uri());
                    throw new OpenDidException(ErrorCode.STATUS_LIST_CREDENTIAL_SUSPENDED);
                }
                case 3 -> {
                    // The issuer's Status List integration guide defines 3 as RESERVED and states that it is "not
                    // treated as a valid credential". Unlike the "application specific" meaning in the IETF draft, this
                    // value is always rejected (the fail-open policy does not apply).
                    log.warn("Credential RESERVED(3) at idx={}, uri={}", ref.idx(), ref.uri());
                    throw new OpenDidException(ErrorCode.STATUS_LIST_CREDENTIAL_RESERVED);
                }
                default -> {
                    // The IETF status list draft defines only 0-3; all other values are reserved. Treating reserved values
                    // as "safe to pass" (a known anti-pattern in CRL/OCSP as well) could hide a revoked status behind a
                    // future spec extension or a corrupted value, so the same fail-open/fail-closed policy as for fetch
                    // failures is applied.
                    if (verifierProperty.getStatusList().isFailOnFetchError()) {
                        log.warn("Unknown status value {} at idx={}, rejecting (FAIL-CLOSED)", status, ref.idx());
                        throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
                    }
                    log.warn("Unknown status value {} at idx={}, treating as VALID (FAIL-OPEN)", status, ref.idx());
                }
            }
        } catch (OpenDidException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new OpenDidException(ErrorCode.STATUS_LIST_INDEX_OUT_OF_BOUNDS);
        } catch (Exception e) {
            log.error("Failed to decode status list: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
        }
    }
}
