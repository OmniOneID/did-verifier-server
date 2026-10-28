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
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseCoreDidUtil;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.VerificationMethod;
import org.omnione.did.oid4vc.oid4vp.util.crypto.MultibaseUtils;
import org.omnione.did.verifier.v1.agent.service.DidDocService;
import org.springframework.stereotype.Component;

import java.security.interfaces.ECPublicKey;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusListTokenVerifier {

    private final DidDocService didDocService;
    private final ObjectMapper objectMapper;

    public StatusListTokenPayload verify(String jwt, String expectedUri) {
        try {
            JWSObject jwsObject = JWSObject.parse(jwt);

            // 1. Resolve the public key from kid
            String kid = jwsObject.getHeader().getKeyID();
            if (kid == null) throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
            ECPublicKey publicKey = resolvePublicKey(kid);

            // 2. Verify the signature
            JWSVerifier verifier = new ECDSAVerifier(publicKey);
            if (!jwsObject.verify(verifier)) {
                log.warn("Status list token signature verification failed for uri: {}", expectedUri);
                throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
            }

            // 3. Parse the payload
            byte[] payloadBytes = jwsObject.getPayload().toBytes();
            JsonNode payload = objectMapper.readTree(payloadBytes);

            // 4. sub == expectedUri
            String sub = payload.path("sub").asText(null);
            if (!expectedUri.equals(sub)) {
                log.warn("Status list token sub mismatch: expected={}, actual={}", expectedUri, sub);
                throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
            }

            // 5. Check expiration (exp)
            long exp = payload.path("exp").asLong(0);
            if (exp > 0 && Instant.now().getEpochSecond() > exp) {
                log.warn("Status list token expired for uri: {}", expectedUri);
                throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
            }

            // 6. Extract the status_list claim
            JsonNode statusList = payload.path("status_list");
            int bits = statusList.path("bits").asInt(1);
            String lst = statusList.path("lst").asText(null);
            long ttl = payload.path("ttl").asLong(0);

            if (lst == null) throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);

            return new StatusListTokenPayload(bits, lst, ttl, exp);

        } catch (OpenDidException e) {
            throw e;
        } catch (Exception e) {
            log.error("Status list token verification failed: {}", e.getMessage());
            throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
        }
    }

    private ECPublicKey resolvePublicKey(String kid) {
        // To handle the "did:omn:issuer?versionId=1#auth" form, a '?' before '#' marks the end of the DID.
        // (Same parsing rules as OID4VPService.extractDid/extractKeyId)
        int queryIdx = kid.indexOf('?');
        int hashIdx = kid.indexOf('#');
        String did;
        if (queryIdx > 0) {
            did = kid.substring(0, queryIdx);
        } else if (hashIdx > 0) {
            did = kid.substring(0, hashIdx);
        } else {
            did = kid;
        }
        String keyId = hashIdx >= 0 ? kid.substring(hashIdx + 1) : "";

        DidDocument didDoc = didDocService.getDidDocument(did);
        VerificationMethod vm = BaseCoreDidUtil.getVerificationMethod(didDoc, keyId);

        try {
            byte[] keyBytes = MultibaseUtils.decodeMultibase(vm.getPublicKeyMultibase());
            return decodeP256PublicKey(keyBytes);
        } catch (Exception e) {
            throw new OpenDidException(ErrorCode.STATUS_LIST_TOKEN_INVALID);
        }
    }

    private ECPublicKey decodeP256PublicKey(byte[] compressed) throws Exception {
        // compressed 33-byte P-256 → ECPublicKey
        org.bouncycastle.jce.spec.ECNamedCurveParameterSpec spec =
                org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec("P-256");
        org.bouncycastle.math.ec.ECPoint point = spec.getCurve().decodePoint(compressed);
        org.bouncycastle.jce.spec.ECPublicKeySpec pubKeySpec =
                new org.bouncycastle.jce.spec.ECPublicKeySpec(point, spec);
        return (ECPublicKey) java.security.KeyFactory.getInstance("EC", "BC")
                .generatePublic(pubKeySpec);
    }
}
