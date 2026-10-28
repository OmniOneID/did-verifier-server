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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.omnione.did.base.datamodel.enums.ProofPurpose;
import org.omnione.did.base.db.constant.TransactionStatus;
import org.omnione.did.base.db.domain.Oid4vpSession;
import org.omnione.did.base.db.domain.Oid4vpSessionMapping;
import org.omnione.did.base.db.domain.Transaction;
import org.omnione.did.base.db.domain.VerifierInfo;
import org.omnione.did.base.db.repository.Oid4vpSessionJpaRepository;
import org.omnione.did.base.db.repository.Oid4vpSessionMappingRepository;
import org.omnione.did.base.exception.ErrorCode;
import org.omnione.did.base.exception.OpenDidException;
import org.omnione.did.base.util.BaseCoreDidUtil;
import org.omnione.did.data.model.did.DidDocument;
import org.omnione.did.data.model.did.VerificationMethod;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.dto.IdentifierResult;
import org.omnione.did.oid4vc.formatter.oid4vp.verifier.impl.MDocVPVerifier;
import org.omnione.did.oid4vc.oid4vp.dto.ServiceResult;
import org.omnione.did.oid4vc.oid4vp.exception.OID4VPException;
import org.omnione.did.oid4vc.oid4vp.service.AuthorizationService;
import org.omnione.did.oid4vc.oid4vp.service.OID4VPHelperService;
import org.omnione.did.oid4vc.oid4vp.util.crypto.JweResponseDecryptor;
import org.omnione.did.oid4vc.oid4vp.util.crypto.MultibaseUtils;
import org.omnione.did.oid4vc.oid4vp.util.jar.jws.CompactSigner;

import java.security.interfaces.ECPrivateKey;
import org.omnione.did.verifier.v1.admin.service.VerifierInfoQueryService;
import org.omnione.did.verifier.v1.agent.service.DidDocService;
import org.omnione.did.verifier.v1.agent.service.FileWalletService;
import org.omnione.did.verifier.v1.agent.service.TransactionService;
import org.omnione.did.verifier.v1.common.service.VpSubmitAuditService;
import org.omnione.did.verifier.v1.protocol.service.status.CredentialStatusChecker;
import org.omnione.did.verifier.v1.protocol.api.dto.Oid4vpResponseRequest;
import org.omnione.did.verifier.v1.protocol.api.dto.Oid4vpResponseResult;
import org.omnione.did.verifier.v1.protocol.security.Oid4vpEncKeyManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class OID4VPService {

    private final AuthorizationService authorizationService;
    private final OID4VPHelperService oid4VPHelperService;
    private final Oid4vpSessionMappingRepository sessionMappingRepository;
    private final Oid4vpSessionJpaRepository oid4vpSessionJpaRepository;
    private final TransactionService transactionService;
    private final FileWalletService fileWalletService;
    private final VerifierInfoQueryService verifierInfoQueryService;
    private final DidDocService didDocService;
    private final ObjectMapper objectMapper;
    private final VpSubmitAuditService vpSubmitAuditService;
    private final Oid4vpEncKeyManager encKeyManager;
    private final JweResponseDecryptor jweResponseDecryptor;
    private final CredentialStatusChecker credentialStatusChecker;

    /**
     * Retrieves the Authorization Request JWT (called by the wallet via request_uri).
     * This verifier always uses DID-based signing (decentralized_identifier scheme), regardless of format.
     * The JWT is signed with the FileWallet assert key.
     */
    public ServiceResult<String> getAuthorizationRequest(String requestId) {
        log.debug("=== OID4VP getAuthorizationRequest for requestId: {} ===", requestId);

        Oid4vpSessionMapping mapping = sessionMappingRepository.findByOid4vpRequestId(requestId)
                .orElseThrow(() -> new OpenDidException(ErrorCode.OID4VP_SESSION_MAPPING_NOT_FOUND));

        Transaction transaction = transactionService.findTransactionByTxId(mapping.getTxId());
        validateTransaction(transaction);

        ServiceResult<String> result = buildDidAuthorizationRequest(requestId);

        if (!result.isSuccess()) {
            log.error("Failed to get authorization request: {} - {}", result.getErrorCode(), result.getErrorDescription());
            throw new OpenDidException(ErrorCode.OID4VP_AUTHORIZATION_REQUEST_FAILED);
        }

        log.debug("*** Authorization request retrieved for requestId: {} ***", requestId);
        return result;
    }

    /** DID-based signing (decentralized_identifier scheme) */
    private ServiceResult<String> buildDidAuthorizationRequest(String requestId) {
        VerifierInfo verifierInfo = verifierInfoQueryService.getVerifierInfo();
        DidDocument verifierDidDoc = didDocService.getDidDocument(verifierInfo.getDid());

        String keyId = ProofPurpose.ASSERTION_METHOD.toKeyId();
        VerificationMethod vm = BaseCoreDidUtil.getVerificationMethod(verifierDidDoc, keyId);
        String verificationMethod = verifierDidDoc.getId() + "?versionId=" + verifierDidDoc.getVersionId() + "#" + vm.getId();
        String publicKeyMultibase = vm.getPublicKeyMultibase();

        // CompactSigner passes a hash that is already SHA-256 digested, so sign with FromHash without rehashing (avoids double hashing).
        CompactSigner compactSigner = (kid, hash) -> fileWalletService.generateCompactSignatureFromHash(kid, hash);

        return authorizationService.getAuthorizationRequest(
                requestId, compactSigner, verificationMethod, publicKeyMultibase);
    }

    /**
     * Processes the VP Token response (vp_token submitted by the wallet).
     * Reads the credential ids of the DCQL query from the session and maps them dynamically to vpTokenMap keys.
     */
    @Transactional
    public Oid4vpResponseResult receiveResponse(Oid4vpResponseRequest request) {
        if (isEncrypted(request)) {
            return receiveEncryptedResponse(request.getResponse());
        }
        rejectIfEncryptionRequired(request.getState());
        return processResponse(request.getState(), request.getVpToken(),
                request.getError(), request.getErrorDescription());
    }

    static boolean isEncrypted(Oid4vpResponseRequest request) {
        return request.getResponse() != null && !request.getResponse().isBlank();
    }

    /**
     * Rejects a plaintext submission without response (JWE) for a session initiated with direct_post.jwt.
     * Without this check, response_mode=direct_post.jwt would only be a request to the wallet that the
     * server does not actually enforce, and a single plaintext submission would defeat the purpose of JWE
     * (keeping claims out of plaintext in logs and proxies).
     * If state is missing or no session is mapped, no decision is made here; processResponse
     * handles it as OID4VP_SESSION_MAPPING_NOT_FOUND.
     */
    private void rejectIfEncryptionRequired(String state) {
        if (state == null || state.isBlank()) return;
        oid4vpSessionJpaRepository.findByState(state).ifPresent(session -> {
            if ("direct_post.jwt".equals(session.getResponseMode())) {
                log.warn("Plaintext response rejected — session requires direct_post.jwt (state={})", state);
                throw new OpenDidException(ErrorCode.OID4VP_ENCRYPTED_RESPONSE_REQUIRED);
            }
        });
    }

    /**
     * Processes a direct_post.jwt (JWE) response.
     * The session and ephemeral private key are identified by kid first, then decryption is delegated to the SDK utility.
     * A failure before the session is found by kid (the first line) cannot be audited because the transaction is unknown.
     * Failures after the session is resolved are recorded in the VpSubmit audit log and mark the transaction FAILED,
     * the same as the plaintext path (processResponse), so that the VP history kept for non-repudiation also covers
     * encrypted responses.
     */
    private Oid4vpResponseResult receiveEncryptedResponse(String jweCompact) {
        String kid = encKeyManager.extractKid(jweCompact);
        Oid4vpSession session = oid4vpSessionJpaRepository.findByEncKid(kid)
                .orElseThrow(() -> new OpenDidException(ErrorCode.OID4VP_SESSION_MAPPING_NOT_FOUND));

        // Oid4vpSession.transactionId is the SDK's internal session identifier and differs from Transaction.txId,
        // so, as in processResponse, the transaction is looked up through Oid4vpSessionMapping (state → txId).
        Oid4vpSessionMapping mapping = sessionMappingRepository.findByState(session.getState())
                .orElseThrow(() -> new OpenDidException(ErrorCode.OID4VP_SESSION_MAPPING_NOT_FOUND));
        Transaction transaction = transactionService.findTransactionByTxId(mapping.getTxId());

        String state;
        String vpTokenJson;
        try {
            JweResponseDecryptor.DecryptedResponse decrypted;
            try {
                ECPrivateKey privateKey = encKeyManager.loadPrivateKey(session.getEncPrivateKeyJwk());
                decrypted = jweResponseDecryptor.decrypt(jweCompact, privateKey);
            } catch (OID4VPException e) {
                log.warn("OID4VP response decryption failed for kid={}: {}", kid, e.getErrorMsg());
                throw new OpenDidException(ErrorCode.OID4VP_RESPONSE_DECRYPTION_FAILED, e);
            }

            Map<String, Object> payload;
            try {
                payload = objectMapper.readValue(decrypted.getPlaintext(), Map.class);
            } catch (JsonProcessingException e) {
                log.warn("Decrypted JWE payload is not valid JSON for kid={}", kid);
                throw new OpenDidException(ErrorCode.OID4VP_RESPONSE_DECRYPTION_FAILED, e);
            }

            // Encrypted error-only responses (a payload with only {error, error_description, state} and no vp_token)
            // are intentionally not supported: such a payload is rejected with OID4VP_RESPONSE_DECRYPTION_FAILED
            // instead of being handled as an OAuth error. The payload contract defines only the success form
            // (vp_token/presentation_submission/state).
            // The plaintext path (processResponse) has the same limitation: it passes error/errorDescription to
            // authorizationService.receiveResponse(...), but always calls oid4VPHelperService.parseVPToken(vpTokenJson)
            // first, and the SDK's OID4VPHelperService.parseVPToken(null) throws OID4VPException(ERR_CODE_VP_TOKEN_NULL)
            // before the error can be handled. The encrypted path simply fails earlier with a different error code.
            // Supporting both vp_token and error responses requires (a) an error-first branch in processResponse and
            // (b) extracting and passing error/error_description in this method.
            Object vpTokenValue = payload.get("vp_token");
            Object stateValue = payload.get("state");
            if (vpTokenValue == null || stateValue == null) {
                log.warn("Decrypted JWE payload missing vp_token/state for kid={}", kid);
                throw new OpenDidException(ErrorCode.OID4VP_RESPONSE_DECRYPTION_FAILED);
            }

            // The public key is exposed through client_metadata, so anyone can encrypt with session A's key while putting
            // another session's state in the payload. Reject when the session found by kid and the decrypted state point to
            // different sessions, to prevent mapping corruption.
            if (!session.getState().equals(stateValue.toString())) {
                log.warn("Decrypted JWE state does not match session bound to kid={}: sessionState={}, payloadState={}",
                        kid, session.getState(), stateValue);
                throw new OpenDidException(ErrorCode.OID4VP_RESPONSE_DECRYPTION_FAILED);
            }

            state = stateValue.toString();
            vpTokenJson = vpTokenValue instanceof String
                    ? (String) vpTokenValue
                    : objectMapper.writeValueAsString(vpTokenValue);
        } catch (OpenDidException e) {
            vpSubmitAuditService.recordFailure(transaction.getId(), null, null, e.getErrorCode().getCode(), null);
            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.FAILED);
            throw e;
        } catch (JsonProcessingException e) {
            OpenDidException wrapped = new OpenDidException(ErrorCode.OID4VP_RESPONSE_DECRYPTION_FAILED, e);
            vpSubmitAuditService.recordFailure(transaction.getId(), null, null, wrapped.getErrorCode().getCode(), null);
            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.FAILED);
            throw wrapped;
        }

        // processResponse already records its own success/failure audit and transaction status
        // (calling it inside the catch scope above would record twice), so it is called outside the try block.
        return processResponse(state, vpTokenJson, null, null);
    }

    /**
     * Verifies the VP Token. Looks up the session mapping by state and maps the result to Transaction/VpSubmit.
     * Reads the credential ids of the DCQL query from the session and maps them dynamically to vpTokenMap keys.
     */
    Oid4vpResponseResult processResponse(String state, String vpTokenJson, String error, String errorDescription) {
        log.debug("=== OID4VP receiveResponse for state: {} ===", state);

        // 1. Look up the session mapping by state (without a mapping the transaction is unknown and VpSubmit cannot be saved)
        Oid4vpSessionMapping mapping = sessionMappingRepository.findByState(state)
                .orElseThrow(() -> new OpenDidException(ErrorCode.OID4VP_SESSION_MAPPING_NOT_FOUND));

        Transaction transaction = transactionService.findTransactionByTxId(mapping.getTxId());

        // Declared outside the try so the catch blocks can use them for auditing. If an exception occurs before they
        // are set inside the try, they remain null, meaning "not yet resolved".
        String holderDid = null;
        String vpFormat = null;
        try {
            // 2. Validate the transaction
            validateTransaction(transaction);

            // 3. Load the DCQL query from oid4vp_session → extract credential IDs dynamically
            Oid4vpSession oid4vpSession = oid4vpSessionJpaRepository.findByState(state)
                    .orElseThrow(() -> new OpenDidException(ErrorCode.OID4VP_SESSION_MAPPING_NOT_FOUND));

            String dcqlQuery = oid4vpSession.getDcqlQuery();
            // vp_token is in the DCQL response format ({credentialId: [VP, ...]}), so it is parsed as-is by the SDK parser.
            // (Mapping it manually would put the whole vp_token under the credentialId key again and wrap it twice.)
            Map<String, List<Object>> vpTokenMap = oid4VPHelperService.parseVPToken(vpTokenJson);
            log.debug("vpTokenMap keys: {}", vpTokenMap.keySet());

            // This verifier accepts only DID(kid)-based credentials. x5chain-based mdoc is rejected here.
            rejectX5cBasedMdoc(vpTokenMap);

            vpFormat = resolveSubmitFormat(dcqlQuery);
            List<String> issuerPublicKeys = resolveIssuerPublicKeys(vpTokenMap);
            List<String> holderPublicKeys = resolveHolderPublicKeys(vpTokenMap);
            holderDid = resolveHolderDid(vpTokenMap).orElse(null);
            log.debug("Resolved issuerPublicKeys: {}, holderPublicKeys: {}, holderDid: {}",
                    issuerPublicKeys.size(), holderPublicKeys.size(), holderDid);

            ServiceResult<Map<String, Object>> result = authorizationService.receiveResponse(
                    vpTokenMap,
                    issuerPublicKeys.isEmpty() ? List.of("unresolved-issuer-key") : issuerPublicKeys,
                    holderPublicKeys.isEmpty() ? null : holderPublicKeys,
                    state,
                    error,
                    errorDescription,
                    "POST"
            );

            if (result.isSuccess()) {
                credentialStatusChecker.checkAll(vpTokenMap);
                vpSubmitAuditService.recordSuccess(transaction.getId(), vpTokenJson, holderDid, vpFormat);
                transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.COMPLETED);
                log.debug("*** OID4VP response processed. txId={}, status=COMPLETED ***", mapping.getTxId());

                return Oid4vpResponseResult.builder()
                        .sessionId(mapping.getTxId())
                        .status("COMPLETED")
                        .build();
            }

            vpSubmitAuditService.recordFailure(transaction.getId(), vpTokenJson, holderDid, result.getErrorCode(), vpFormat);
            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.FAILED);
            log.error("OID4VP response failed: {} - {}", result.getErrorCode(), result.getErrorDescription());

            return Oid4vpResponseResult.builder()
                    .sessionId(mapping.getTxId())
                    .status("FAILED")
                    .error(result.getErrorCode())
                    .errorDescription(result.getErrorDescription())
                    .build();

        } catch (OpenDidException e) {
            vpSubmitAuditService.recordFailure(transaction.getId(), vpTokenJson, holderDid, e.getErrorCode().getCode(), vpFormat);
            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.FAILED);
            throw e;
        } catch (OID4VPException e) {
            // SDK processing failure such as vp_token parsing: record the failure and return a FAILED result (the controller maps it to 400)
            log.error("OID4VP response failed: {} - {}", e.getErrorCode(), e.getErrorMsg(), e);
            vpSubmitAuditService.recordFailure(transaction.getId(), vpTokenJson, holderDid, e.getErrorCode(), vpFormat);
            transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.FAILED);
            return Oid4vpResponseResult.builder()
                    .sessionId(mapping.getTxId())
                    .status("FAILED")
                    .error(e.getErrorCode())
                    .errorDescription(e.getErrorMsg())
                    .build();
        }
    }

    /**
     * Extracts the holder DID from vpTokenMap by parsing the format-specific verificationMethod/kid and keeping only the DID part.
     * If there are multiple credentials, the first DID that can be resolved is returned.
     */
    private Optional<String> resolveHolderDid(Map<String, List<Object>> vpTokenMap) {
        if (vpTokenMap == null || vpTokenMap.isEmpty()) return Optional.empty();
        for (List<Object> credentials : vpTokenMap.values()) {
            if (credentials == null) continue;
            for (Object credential : credentials) {
                VpTokenFormat format = detectFormat(credential);
                Optional<String> did = switch (format) {
                    case JSON_VP -> extractHolderDidFromJsonVp(credential);
                    case SD_JWT -> extractHolderDidFromSdJwt((String) credential);
                    default -> Optional.empty();
                };
                if (did.isPresent()) return did;
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractHolderDidFromJsonVp(Object credential) {
        try {
            JsonNode vp = toJsonNode(credential);
            JsonNode proof = vp.get("proof");
            if (proof == null) return Optional.empty();
            JsonNode vmNode = proof.get("verificationMethod");
            if (vmNode == null) return Optional.empty();
            return Optional.of(extractDid(vmNode.asText()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> extractHolderDidFromSdJwt(String sdJwt) {
        try {
            String issuerJwt = sdJwt.split("~", 2)[0];
            String[] jwtParts = issuerJwt.split("\\.");
            if (jwtParts.length < 2) return Optional.empty();
            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(jwtParts[1]));
            JsonNode cnf = payload.get("cnf");
            if (cnf == null) return Optional.empty();
            JsonNode kidNode = cnf.get("kid");
            if (kidNode == null) return Optional.empty();
            return Optional.of(extractDid(kidNode.asText()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Iterates over the credentials in vpTokenMap and resolves issuer public keys with a format-specific resolver.
     * The returned order matches the SDK's keyIndex-based mapping (flattened credential order).
     *
     * Supported formats:
     *   - JSON_VP   (OpenDID / W3C VC JSON-LD): verifiableCredential[].proof.verificationMethod
     *   - SD_JWT    (IETF SD-JWT VC): kid of the issuer JWS header
     *   - MDOC      (ISO/IEC 18013-5): kid (DID URL) of IssuerAuth
     */
    private List<String> resolveIssuerPublicKeys(Map<String, List<Object>> vpTokenMap) {
        List<String> keys = new ArrayList<>();
        if (vpTokenMap == null || vpTokenMap.isEmpty()) return keys;
        for (List<Object> credentials : vpTokenMap.values()) {
            if (credentials == null) continue;
            for (Object credential : credentials) {
                VpTokenFormat format = detectFormat(credential);
                switch (format) {
                    case JSON_VP -> keys.addAll(resolveIssuerKeysFromJsonVp(credential));
                    case SD_JWT -> resolveIssuerKeyFromSdJwt((String) credential).ifPresent(keys::add);
                    case MDOC -> resolveIssuerKeyFromMdoc((String) credential).ifPresent(keys::add);
                    case UNKNOWN -> log.warn("Unknown VP token format; skipping issuer key resolution");
                }
            }
        }
        return keys;
    }

    /**
     * Iterates over the credentials in vpTokenMap and resolves holder public keys with a format-specific resolver.
     * Credentials without a holder key (e.g. SD-JWT without key binding) are skipped.
     */
    private List<String> resolveHolderPublicKeys(Map<String, List<Object>> vpTokenMap) {
        List<String> keys = new ArrayList<>();
        if (vpTokenMap == null || vpTokenMap.isEmpty()) return keys;
        for (List<Object> credentials : vpTokenMap.values()) {
            if (credentials == null) continue;
            for (Object credential : credentials) {
                VpTokenFormat format = detectFormat(credential);
                switch (format) {
                    case JSON_VP -> resolveHolderKeyFromJsonVp(credential).ifPresent(keys::add);
                    case SD_JWT -> resolveHolderKeyFromSdJwt((String) credential).ifPresent(keys::add);
                    // mdoc holder binding (DeviceAuth) is verified with the deviceKey in the MSO, so there is no key to resolve.
                    case MDOC -> log.debug("mDoc holder key is taken from the MSO deviceKey; nothing to resolve");
                    case UNKNOWN -> log.warn("Unknown VP token format; skipping holder key resolution");
                }
            }
        }
        return keys;
    }

    /**
     * Detects the format from the shape of the credential.
     *   - String starting with '{': JSON VP (serialized string)
     *   - String containing '~': SD-JWT (compact format with disclosures)
     *   - Map: JSON VP (parsed object)
     *   - Any other base64url String: MDOC (base64url-CBOR)
     */
    private VpTokenFormat detectFormat(Object credential) {
        if (credential instanceof String s) {
            String t = s.trim();
            if (t.startsWith("{")) return VpTokenFormat.JSON_VP;
            if (t.contains("~")) return VpTokenFormat.SD_JWT;
            // base64url without separators → likely MDOC CBOR
            if (t.matches("[A-Za-z0-9_=-]+")) return VpTokenFormat.MDOC;
            return VpTokenFormat.UNKNOWN;
        }
        if (credential instanceof Map) return VpTokenFormat.JSON_VP;
        return VpTokenFormat.UNKNOWN;
    }

    /**
     * Extracts the representative format value from the DCQL query. Used to store VpSubmit.format.
     * When multiple formats are mixed, the first one, credentials[0].format, is returned.
     */
    private String resolveSubmitFormat(String dcqlQueryJson) {
        if (dcqlQueryJson == null) return null;
        try {
            JsonNode dcql = objectMapper.readTree(dcqlQueryJson);
            JsonNode credentials = dcql.get("credentials");
            if (credentials != null && credentials.isArray() && !credentials.isEmpty()) {
                JsonNode formatNode = credentials.get(0).get("format");
                if (formatNode != null && !formatNode.isNull()) {
                    return formatNode.asText();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to resolve submit format from DCQL query: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Rejects an mso_mdoc credential that is based on x5chain.
     *
     * <p>This verifier supports only DID(kid)-based mdoc. x5c verification remains in the SDK (for interoperability
     * with other wallets), but the server does not operate a trust anchor (IACA), so an mdoc carrying only x5chain
     * is rejected explicitly here, before it fails inside the SDK with a misleading message such as
     * "Trusted root certificates required".
     */
    private void rejectX5cBasedMdoc(Map<String, List<Object>> vpTokenMap) {
        if (vpTokenMap == null) return;
        for (List<Object> credentials : vpTokenMap.values()) {
            if (credentials == null) continue;
            for (Object credential : credentials) {
                if (detectFormat(credential) != VpTokenFormat.MDOC) continue;
                if (isX5cBasedMdoc((String) credential)) {
                    log.warn("Rejected x5chain-based mso_mdoc — this verifier accepts DID(kid)-based mdoc only");
                    throw new OpenDidException(ErrorCode.OID4VP_MDOC_X5C_NOT_SUPPORTED);
                }
            }
        }
    }

    /** Determines whether the mso_mdoc identifies its issuer only by x5chain, without a kid (DID). */
    static boolean isX5cBasedMdoc(String mdocBase64) {
        try {
            IdentifierResult issuer = new MDocVPVerifier().extractIssuerIdentifier(mdocBase64);
            return issuer != null && issuer.getType() == IdentifierResult.Type.MSO_MDOC_X5C;
        } catch (Exception e) {
            log.warn("Failed to classify mso_mdoc issuer identifier: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Resolves the issuer public key from the kid (DID URL) of the mso_mdoc IssuerAuth.
     * As in the SD-JWT kid path, the publicKeyMultibase of the DID Document is converted to base64 and passed to the SDK.
     */
    private Optional<String> resolveIssuerKeyFromMdoc(String mdocBase64) {
        try {
            IdentifierResult issuer = new MDocVPVerifier().extractIssuerIdentifier(mdocBase64);
            if (issuer == null || issuer.getType() != IdentifierResult.Type.MSO_MDOC_KID) {
                log.warn("mso_mdoc has no kid(DID); cannot resolve issuer public key");
                return Optional.empty();
            }
            return resolveKeyByVerificationMethod(issuer.getValue());
        } catch (Exception e) {
            log.warn("Failed to resolve issuer public key from mso_mdoc: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private List<String> resolveIssuerKeysFromJsonVp(Object credential) {
        List<String> keys = new ArrayList<>();
        try {
            JsonNode vp = toJsonNode(credential);
            JsonNode vcs = vp.get("verifiableCredential");
            if (vcs == null || !vcs.isArray()) return keys;
            for (JsonNode vc : vcs) {
                JsonNode proof = vc.get("proof");
                if (proof == null) continue;
                JsonNode vmNode = proof.get("verificationMethod");
                if (vmNode == null) continue;
                resolveKeyByVerificationMethod(vmNode.asText()).ifPresent(keys::add);
            }
        } catch (Exception e) {
            log.warn("Failed to parse JSON VP for issuer key resolution: {}", e.getMessage());
        }
        return keys;
    }

    private Optional<String> resolveHolderKeyFromJsonVp(Object credential) {
        try {
            JsonNode vp = toJsonNode(credential);
            JsonNode proof = vp.get("proof");
            if (proof == null) return Optional.empty();
            JsonNode vmNode = proof.get("verificationMethod");
            if (vmNode == null) return Optional.empty();
            return resolveKeyByVerificationMethod(vmNode.asText());
        } catch (Exception e) {
            log.warn("Failed to parse JSON VP for holder key resolution: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Resolves the issuer public key from the kid (DID URL) of the SD-JWT issuer JWS header.
     * Format: issuerJwt ~ disclosure1 ~ ... ~ kbJwt
     */
    private Optional<String> resolveIssuerKeyFromSdJwt(String sdJwt) {
        try {
            String issuerJwt = sdJwt.split("~", 2)[0];
            String[] jwtParts = issuerJwt.split("\\.");
            if (jwtParts.length < 2) return Optional.empty();
            JsonNode header = objectMapper.readTree(Base64.getUrlDecoder().decode(jwtParts[0]));
            JsonNode kidNode = header.get("kid");
            if (kidNode == null || kidNode.isNull()) {
                log.warn("SD-JWT header has no 'kid'; cannot resolve issuer public key");
                return Optional.empty();
            }
            return resolveKeyByVerificationMethod(kidNode.asText());
        } catch (Exception e) {
            log.warn("Failed to resolve issuer public key from SD-JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Resolves the holder public key from cnf.kid (DID URL) of the SD-JWT issuer payload.
     * cnf.jwk/x5c are not resolved through DIDs, so an empty result is returned for them.
     */
    private Optional<String> resolveHolderKeyFromSdJwt(String sdJwt) {
        try {
            String issuerJwt = sdJwt.split("~", 2)[0];
            String[] jwtParts = issuerJwt.split("\\.");
            if (jwtParts.length < 2) return Optional.empty();
            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(jwtParts[1]));
            JsonNode cnf = payload.get("cnf");
            if (cnf == null || cnf.isNull()) return Optional.empty();
            JsonNode kidNode = cnf.get("kid");
            if (kidNode == null || kidNode.isNull()) {
                log.debug("SD-JWT cnf has no 'kid'; holder key resolution skipped");
                return Optional.empty();
            }
            return resolveKeyByVerificationMethod(kidNode.asText());
        } catch (Exception e) {
            log.warn("Failed to resolve holder public key from SD-JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> resolveKeyByVerificationMethod(String verificationMethod) {
        String did = extractDid(verificationMethod);
        String keyId = extractKeyId(verificationMethod);
        try {
            DidDocument didDoc = didDocService.getDidDocument(did);
            VerificationMethod vm = BaseCoreDidUtil.getVerificationMethod(didDoc, keyId);
            String base64Key = multibaseToBase64(vm.getPublicKeyMultibase());
            log.debug("Resolved public key for DID: {}, keyId: {}", did, keyId);
            return Optional.of(base64Key);
        } catch (Exception e) {
            log.warn("Failed to resolve public key [{}#{}]: {}", did, keyId, e.getMessage());
            return Optional.empty();
        }
    }

    private JsonNode toJsonNode(Object credential) throws java.io.IOException {
        if (credential instanceof String s) return objectMapper.readTree(s);
        return objectMapper.valueToTree(credential);
    }

    /** VP Token credential format identifiers. Additional formats are added here. */
    private enum VpTokenFormat {
        JSON_VP,
        SD_JWT,
        MDOC,
        UNKNOWN
    }

    /**
     * Extracts the DID part from a verificationMethod.
     * "did:omn:holder?versionId=1#auth" → "did:omn:holder"
     */
    private String extractDid(String verificationMethod) {
        int queryIdx = verificationMethod.indexOf('?');
        int hashIdx = verificationMethod.indexOf('#');
        if (queryIdx > 0) return verificationMethod.substring(0, queryIdx);
        if (hashIdx > 0) return verificationMethod.substring(0, hashIdx);
        return verificationMethod;
    }

    /**
     * Extracts the keyId part from a verificationMethod.
     * "did:omn:holder?versionId=1#auth" → "auth"
     */
    private String extractKeyId(String verificationMethod) {
        int hashIdx = verificationMethod.indexOf('#');
        if (hashIdx >= 0) return verificationMethod.substring(hashIdx + 1);
        return "";
    }

    /**
     * Converts the publicKeyMultibase of a DID Document into the Base64 form (33-byte compressed secp256r1 key)
     * expected by OpenDIDVPVerifier.validateSignature().
     *
     * Supported multibase prefixes:
     *   'm' → base64 standard
     *   'u' → base64url
     *   'z' → base58btc (via MultibaseUtils)
     */
    private String multibaseToBase64(String publicKeyMultibase) throws Exception {
        byte[] keyBytes = MultibaseUtils.decodeMultibase(publicKeyMultibase);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new OpenDidException(ErrorCode.TRANSACTION_INVALID);
        }
        if (transaction.getExpired_at().isBefore(java.time.Instant.now())) {
            throw new OpenDidException(ErrorCode.TRANSACTION_EXPIRED);
        }
    }
}
