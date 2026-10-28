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

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.mdoc.core.oid4vp.MDocParser;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Extracts the status list reference from the MSO of an mso_mdoc.
 *
 * <p>The structure is the same as the SD-JWT status claim, but CBOR-encoded inside the payload (MSO)
 * of IssuerAuth (COSE_Sign1).
 *
 * <pre>
 * MSO = { ..., "status": { "status_list": { "idx": 6, "uri": "https://.../status-lists/2" } } }
 * </pre>
 *
 * <p>Only the reference is extracted here. The authenticity of the MSO itself is already guaranteed by the
 * IssuerAuth signature verification (SDK), so the revocation status lookup and verification go through exactly
 * the same pipeline as SD-JWT ({@link CredentialStatusChecker}).
 */
@Slf4j
@Component
public class MdocStatusClaimParser {

    private static final String ISSUER_SIGNED = "issuerSigned";
    private static final String ISSUER_AUTH = "issuerAuth";
    private static final String STATUS = "status";
    private static final String STATUS_LIST = "status_list";
    private static final int COSE_SIGN1_PAYLOAD_INDEX = 2;
    private static final int MAX_BYTE_STRING_UNWRAP = 3;

    public Optional<StatusListRef> parse(String mdocBase64) {
        try {
            CBORObject mso = extractMso(mdocBase64);
            if (mso == null) return Optional.empty();

            CBORObject status = mso.get(STATUS);
            if (status == null || status.getType() != CBORType.Map) return Optional.empty();

            CBORObject statusList = status.get(STATUS_LIST);
            if (statusList == null || statusList.getType() != CBORType.Map) return Optional.empty();

            CBORObject idx = statusList.get("idx");
            CBORObject uri = statusList.get("uri");
            if (idx == null || uri == null) return Optional.empty();

            return Optional.of(new StatusListRef(idx.AsInt32Value(), uri.AsString()));
        } catch (Exception e) {
            log.debug("Failed to parse status claim from mso_mdoc: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Unwraps the IssuerAuth (COSE_Sign1) payload and returns the MSO map. */
    private CBORObject extractMso(String mdocBase64) throws Exception {
        CBORObject document = MDocParser.extractFirstDocument(mdocBase64);

        CBORObject issuerSigned = document.get(ISSUER_SIGNED);
        if (issuerSigned == null) return null;

        CBORObject issuerAuth = issuerSigned.get(ISSUER_AUTH);
        if (issuerAuth == null) return null;
        if (issuerAuth.isTagged()) {
            // COSE_Sign1 may arrive wrapped in tag 18.
            issuerAuth = issuerAuth.Untag();
        }
        if (issuerAuth.getType() != CBORType.Array || issuerAuth.size() <= COSE_SIGN1_PAYLOAD_INDEX) {
            return null;
        }

        // The payload arrives as tag 24 (CBOR wrapped in a bstr).
        CBORObject mso = issuerAuth.get(COSE_SIGN1_PAYLOAD_INDEX);
        for (int i = 0; i < MAX_BYTE_STRING_UNWRAP && mso != null
                && mso.getType() == CBORType.ByteString; i++) {
            mso = CBORObject.DecodeFromBytes(mso.GetByteString());
        }
        if (mso != null && mso.isTagged()) {
            mso = mso.Untag();
        }
        return mso != null && mso.getType() == CBORType.Map ? mso : null;
    }
}
