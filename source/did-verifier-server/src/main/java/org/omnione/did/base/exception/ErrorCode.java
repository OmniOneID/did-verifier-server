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

package org.omnione.did.base.exception;

import lombok.Getter;

/**
 * Enumeration of error codes used in the DID Verifier system.
 * Each error code contains a unique identifier, a descriptive message, and an associated HTTP status code.
 *
 */
@Getter
public enum ErrorCode {

    // General Errors (100-199)
    REQUEST_BODY_UNREADABLE("SSRVVRF00100", "Unable to process the request.", 400),
    JSON_PARSE_ERROR("SSRVVRF00101", "Failed to parse JSON.", 500),
    BLOCKCHAIN_GET_DID_DOC_FAILED("SSRVVRF00102", "Failed to retrieve DID document on the blockchain.", 500),
    SIGNATURE_VERIFICATION_FAILED("SSRVVRF00103", "Failed to verify signature.", 500),
    GENERATE_HASH_FAILED("SSRVVRF00104", "Failed to generate hash.", 500),
    ENCODING_FAILED("SSRVVRF00105", "Failed to encode data.", 500),
    DECODING_FAILED("SSRVVRF00106", "Failed to decode data : incorrect encoding", 400),
    SIGNATURE_GENERATION_FAILED("SSRVVRF00107", "Failed to generate signature.", 500),
    URL_PING_ERROR("SSRVVRF00108", "Failed to ping the URL.", 400),

    // VP Related Errors (200-299)
    VP_OFFER_NOT_FOUND("SSRVVRF00200", "VP_OFFER is not found.", 400),
    VP_POLICY_NOT_FOUND("SSRVVRF00201", "VP_POLICY is not found.", 400),
    VP_VERIFY_ERROR("SSRVVRF00202", "VP verification failed.", 500),
    VP_PROFILE_NOT_FOUND("SSRVVRF00203", "VP_PROFILE is not found.", 500),
    VP_PROFILE_PARSE_ERROR("SSRVVRF00204", "Failed to parse VP profile.", 500),
    VERIFY_PROFILE_PARSE_ERROR("SSRVVRF00205", "Failed to parse verify profile.", 500),
    VP_POLICY_READ_ERROR("SSRVVRF00206", "Failed to read VP policy.", 500),
    VP_PAYLOAD_NOT_FOUND("SSRVVRF00207", "VP_PAYLOAD is not found.", 400),
    VP_POLICY_PROFILE_NOT_FOUND("SSRVVRF00208", "VP_POLICY_PROFILE is not found.", 400),
    VP_PROCESS_NOT_FOUND("SSRVVRF00209", "VP_PROCESS is not found.", 400),
    VP_FILTER_NOT_FOUND("SSRVVRF00210", "VP_FILTER is not found.", 400),
    VP_POLICY_UPDATE_FAILED("SSRVVRF00211", "Failed to update VP policy.", 500),
    VP_PAYLOAD_IN_USE("SSRVVRF00212", "PAYLOAD is in use by one or more policies",400 ),
    VP_POLICY_PROFILE_IN_USE("SSRVVRF00213", "POLICY_PROFILE is in use by one or more policies",400 ),
    VP_FILTER_IN_USE("SSRVVRF00214", "VP_FILTER is in use by one or more profile",400 ),
    VP_PROCESS_IN_USE("SSRVVRF00215", "VP_PROCESS is in use by one or more profile.", 400),
    VC_SCHEMA_NOT_FOUND("SSRVVRF00216", "VC_SCHEMA not found",500 ),


    // Transaction Errors (300-399)
    TRANSACTION_NOT_FOUND("SSRVVRF00300", "Transaction not found.", 400),
    TRANSACTION_INVALID("SSRVVRF00301", "Transaction status is not pending.", 400),
    TRANSACTION_EXPIRED("SSRVVRF00302", "Transaction has expired.", 400),
    SUB_TRANSACTION_NOT_FOUND("SSRVVRF00303", "Subtransaction not found.", 400),
    SUB_TRANSACTION_INVALID("SSRVVRF00304", "Subtransaction status is invalid.", 400),

    // Authentication and Cryptography Errors (400-499)
    INVALID_AUTH_TYPE("SSRVVRF00400", "Invalid AuthType: Type mismatch.", 400),
    CRYPTO_ERROR("SSRVVRF00401", "Crypto error occurred.", 500),
    INVALID_NONCE("SSRVVRF00402", "Invalid nonce.", 400),
    INVALID_PROOF_PURPOSE("SSRVVRF00403", "Invalid proof purpose.", 400),
    CRYPTO_NONCE_MERGE_FAILED("SSRVVRF00404", "Failed to merge nonce.", 500),
    CRYPTO_SHARED_SECRET_GENERATION_FAILED ("SSRVVRF00405", "Failed to generate shared secret.", 500),
    CRYPTO_SHARED_SECRET_NONCE_MERGE_FAILED("SSRVVRF00406", "Failed to merge shared secret and nonce.", 500),
    GENERATE_NONCE_FAILED("SSRVVRF00407", "Failed to generate nonce.", 500),
    CRYPTO_KEY_PAIR_GENERATION_FAILED("SSRVVRF00408", "Failed to generate key pair.", 500),
    CRYPTO_DECRYPTION_FAILED("SSRVVRF00409", "Failed to decrypt data.", 500),
    INVALID_ECC_CURVE_TYPE("SSRVVRF00410", "Invalid ECC curve type.", 500),
    INVALID_SYMMETRIC_CIPHER_TYPE("SSRVVRF00411", "Invalid symmetric cipher type.", 500),
    INVALID_SYMMETRIC_PADDING_TYPE("SSRVVRF00412", "Invalid symmetric padding type.", 500),

    // Wallet and Key Management Errors (500-599)
    WALLET_CONNECTION_FAILED("SSRVVRF00500", "Failed to connect to wallet.", 500),
    WALLET_SIGNATURE_GENERATION_FAILED("SSRVVRF00501", "Failed to generate wallet signature.", 500),
    PUBLIC_KEY_COMPRESS_FAILED("SSRVVRF00502", "Failed to compress public key.", 500),
    FAILED_TO_GET_FILE_WALLET_MANAGER("SSRVVRF00503", "Failed to get File Wallet Manager.", 500),
    WALLET_ALREADY_EXISTS("SSRVVRF00504", "Failed to create wallet: wallet already exists.", 500),
    INVALID_WALLET_FILE_PATH("SSRVVRF00505", "Failed to create wallet: invalid wallet file path.", 500),
    KEY_ALREADY_EXISTS("SSRVVRF00506", "Failed to generate keys: key already exists.", 500),
    FAILED_TO_LOAD_KEY_ELEMENT("SSRVVRF00507", "Failed to load key element.", 500),
    WALLET_CREATION_FAILED("SSRVVRF00508", "Failed to create wallet.", 500),

    // DID Related Errors (600-699)
    DID_DOCUMENT_RETRIEVAL_FAILED("SSRVVRF00600", "Failed to retrieve DID Document.", 500),
    FAILED_TO_FIND_DID_DOC("SSRVVRF00601", "Failed to find DID document.", 500),
    INVALID_DID_DOCUMENT("SSRVVRF00602", "Invalid DID Document.", 400),
    VERIFIER_ALREADY_REGISTERED("SSRVVRF00603", "Verifier is already registered.", 400),
    VERIFIER_DID_DOCUMENT_ALREADY_REGISTERED("SSRVVRF00604", "Failed to register Verifier DID Document: document is already registered.", 400),
    DIDDOC_GENERATION_FAILED("SSRVVRF00605", "Failed to generate DID document.", 500),
    FAILED_TO_REGISTER_VERIFIER_DID_DOCUMENT("SSRVVRF00606", "Failed to register Verifier DID Document.", 500),
    VERIFIER_DID_DOCUMENT_NOT_FOUND("SSRVVRF00607", "Failed to find Verifier DID Document: o registration request has been made.", 400),
    VERIFIER_DID_DOCUMENT_ALREADY_REQUESTED("SSRVVRF00608", "Failed to register Verifier DID Document: document is already requested.", 400),
    INVALID_CERTIFICATE_VC_JSON_FORMAT("SSRVVRF00609", "Failed to process certificate VC: invalid JSON format.", 500),

    // E2E Related Errors (700-799)
    E2E_NOT_FOUND("SSRVVRF00700", "E2E is not found.", 400),
    ACC_E2E_ERROR("SSRVVRF00701", "E2E is invalid.", 400),
    KEY_GENERATION_FAILED("SSRVVRF00702", "Failed to generate key.", 500),

    // Certificate VC Errors (800~899)
    CERTIFICATE_DATA_NOT_FOUND("SSRVVRF00800", "Certificate VC data not found.", 500),

    // API Process Errors (900~999)
    FAILED_TO_REQUEST_OFFER_QR("SSRVVRF00900", "Failed to process the 'request-offer-qr' API request.", 500),
    FAILED_TO_CONFIRM_VERIFY("SSRVVRF00901", "Failed to process the 'confirm-verify' API request.", 500),
    FAILED_TO_REQUEST_PROFILE("SSRVVRF00902", "Failed to process the 'request-profile' API request.", 500),
    FAILED_TO_REQUEST_VERIFY("SSRVVRF00903", "Failed to process the 'request-verify' API request.", 500),
    FAILED_TO_REQUEST_CERTIFICATE_VC("SSRVVRF00904", "Failed to process the 'get-certificate-vc' API request.", 500),
    FAILED_TO_ISSUE_CERTIFICATE_VC("SSRVVRF00905", "Failed to process the 'issue-certificate-vc' API request.", 500),
    FAILED_TO_REQUEST_PROOF_REQUEST_PROFILE("SSRVVRF00906", "Failed to process the 'request-proof-request-profile' API request.", 500),

    // Verifier Errors (1000~1099)
    VERIFIER_NOT_FOUND("SSRVVRF01000", "Failed to find verifier: verifier is not registered.", 500),

    // Admin Errors (1100~1199)
    ADMIN_INFO_NOT_FOUND("SSRVVRF01100", "Failed to find admin: admin is not registered.", 400),
    ADMIN_ALREADY_EXISTS("SSRVVRF01101", "Failed to register admin: admin is already registered.", 400),
    TAS_COMMUNICATION_ERROR("SSRVVRF01102", "Failed to communicate with tas: unknown error occurred.", 500),
    TAS_UNKNOWN_RESPONSE("SSRVVRF01103", "Failed to process response: received unknown data from the tas.", 500),

    //ZKP Errors (1200~1299)
    PROOF_REQUEST_PROFILE_NOT_FOUND("SSRVVRF01201", "Failed to find Proof request profile : request proof profile not found" , 400),
    ZKP_POLICY_PROFILE_NOT_FOUND("SSRVVRF01202", "Failed to find ZKP policy profile : request proof profile not found" , 400),
    PROOF_REQUEST_PROFILE_PARSE_ERROR("SSRVVRF01203", "Failed to parse proof request profile : request proof profile parse error" , 500),
    ZKP_PROOF_REQUEST_NOT_FOUND("SSRVVRF01204", "Failed to find ZKP proof request : request proof profile not found" , 400),
    FAILED_TO_VERIFY_PROOF("SSRVVRF01205", "Failed to verify proof : proof verify failed" , 500),
    BLOCKCHAIN_GET_ZKP_CREDENTIAL_FAILED("SSRVVRF01206", "Failed to retrieve ZKP credential on the blockchain.", 500),
    BLOCKCHAIN_GET_ZKP_CREDENTIAL_DEFINITION_FAILED("SSRVVRF01207", "Failed to retrieve ZKP credential definition on the blockchain.", 500),
    CREDENTIAL_SCHEMA_NOT_FOUND("SSRVVRF01208", "Failed to find credential schema : credential schema not found" , 400),
    PROOF_REQUEST_NOT_FOUND("SSRVVRF01209", "Failed to find proof request : proof request not found" , 400),
    VC_STAUS_NOT_VALID("SSRVVRF01210", "VC Status id not valid" , 400);

    private final String code;
    private final String message;
    private final int httpStatus;

    ErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public static String getMessageByCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode.getMessage();
            }
        }
        return "Unknown error code: " + code;
    }
}