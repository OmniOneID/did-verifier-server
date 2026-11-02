---
puppeteer:
    pdf:
        format: A4
        displayHeaderFooter: true
        landscape: false
        scale: 0.8
        margin:
            top: 1.2cm
            right: 1cm
            bottom: 1cm
            left: 1cm
    image:
        quality: 100
        fullPage: false
---

Verifier Server Error
==

- Date: 2026-11-02
- Version: v3.0.0

| Version | Date       | Changes                 |
|---------|------------|-------------------------|
| v1.0.0  | 2024-09-03 | Initial version         |
| v2.0.0  | 2025-05-28 | ADD ZKP & ADMIN ERROR   |
| v3.0.0  | 2026-11-02 | Added protocol/OID4VP, status list, and server configuration errors |


<div style="page-break-after: always;"></div>

# Table of Contents
- [Model](#model)
  - [Error Response](#error-response)
- [Error Code](#error-code)
  - [1-1. General Errors (100-199)](#1-1-general-errors-100-199)
  - [1-2. VP Related Errors (200-299)](#1-2-vp-related-errors-200-299)
  - [1-3. Transaction Errors (300-399)](#1-3-transaction-errors-300-399)
  - [1-4. Authentication and Cryptography Errors (400-499)](#1-4-authentication-and-cryptography-errors-400-499)
  - [1-5. Wallet and Key Management Errors (500-599)](#1-5-wallet-and-key-management-errors-500-599)
  - [1-6. DID Related Errors (600-699)](#1-6-did-related-errors-600-699)
  - [1-7. E2E Related Errors (700-799)](#1-7-e2e-related-errors-700-799)
  - [1-8. Certificate VC Errors (800-899)](#1-8-certificate-vc-errors-800-899)
  - [1-9. API Process Errors (900-999)](#1-9-api-process-errors-900-999)
  - [1-10. Verifier Errors (1000-1099)](#1-10-verifier-errors-1000-1099)
  - [1-11. Admin Errors (1100-1199)](#1-11-admin-errors-1100-1199)
  - [1-12. ZKP Errors (1200-1299)](#1-12-zkp-errors-1200-1299)
  - [1-13. Protocol and OID4VP Errors (1300-1399)](#1-13-protocol-and-oid4vp-errors-1300-1399)
  - [1-14. Status List Errors (1400-1499)](#1-14-status-list-errors-1400-1499)
  - [1-15. Server Configuration Errors (21500-21599)](#1-15-server-configuration-errors-21500-21599)

# Model
## Error Response

### Description
```
Error struct for Verifier Backend. It has code and message pair.
Code starts with SSRVVRF.
```

### Declaration
```java
public class ErrorResponse {
    private final String code;
    private final String description;
}
```

### Property

| Name        | Type   | Description                        | **M/O** | **Note** |
|-------------|--------|------------------------------------|---------| -------- |
| code        | String | Error code. It starts with SSRVVRF | M       |          | 
| message     | String | Error description                  | M       |          | 

# Error Code

## 1-1. General Errors (100-199)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00100 | Unable to process the request.                 | 400         | Verify request format and content.             |
| SSRVVRF00101 | Failed to parse JSON.                          | 500         | Check JSON format and structure.               |
| SSRVVRF00102 | Failed to retrieve DID document on the blockchain. | 500    | Check blockchain connection and DID validity.  |
| SSRVVRF00103 | Failed to verify signature.                    | 500         | Verify signature generation process.           |
| SSRVVRF00104 | Failed to generate hash.                       | 500         | Check hashing algorithm and input data.        |
| SSRVVRF00105 | Failed to encode data.                         | 500         | Verify encoding process and input data.        |
| SSRVVRF00106 | Failed to decode data : incorrect encoding     | 400         | Check encoded data and decoding method.        |
| SSRVVRF00107 | Failed to generate signature.                  | 500         | Verify signature generation process and keys.  |
| SSRVVRF00108 | Failed to ping the URL.                        | 400         | Check URL accessibility and network connection. |

<br>

## 1-2. VP Related Errors (200-299)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00200 | VP_OFFER is not found.                         | 400         | Check VP offer existence and validity.         |
| SSRVVRF00201 | VP_POLICY is not found.                        | 400         | Verify VP policy configuration.                |
| SSRVVRF00202 | VP verification failed.                        | 500         | Check VP format and verification process.      |
| SSRVVRF00203 | VP_PROFILE is not found.                       | 500         | Verify VP profile existence and configuration. |
| SSRVVRF00204 | Failed to parse VP profile.                    | 500         | Check VP profile format and structure.         |
| SSRVVRF00205 | Failed to parse verify profile.                | 500         | Check verify profile format and structure.     |
| SSRVVRF00206 | Failed to read VP policy.                      | 500         | Check VP policy data and access permissions.   |
| SSRVVRF00207 | VP_PAYLOAD is not found.                       | 400         | Verify VP payload existence and format.        |
| SSRVVRF00208 | VP_POLICY_PROFILE is not found.                | 400         | Check VP policy profile configuration.         |
| SSRVVRF00209 | VP_PROCESS is not found.                       | 400         | Verify VP process configuration.               |
| SSRVVRF00210 | VP_FILTER is not found.                        | 400         | Check VP filter configuration.                 |
| SSRVVRF00211 | Failed to update VP policy.                    | 500         | Check VP policy update process and permissions. |
| SSRVVRF00212 | PAYLOAD is in use by one or more policies      | 400         | Remove payload dependencies before deletion.   |
| SSRVVRF00213 | POLICY_PROFILE is in use by one or more policies | 400       | Remove policy profile dependencies before deletion. |
| SSRVVRF00214 | VP_FILTER is in use by one or more profile     | 400         | Remove filter dependencies before deletion.    |
| SSRVVRF00215 | VP_PROCESS is in use by one or more profile.   | 400         | Remove process dependencies before deletion.   |
| SSRVVRF00216 | VC_SCHEMA not found                            | 500         | Check VC schema existence and configuration.   |

<br>

## 1-3. Transaction Errors (300-399)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00300 | Transaction not found.                         | 400         | Check transaction ID and existence.            |
| SSRVVRF00301 | Transaction status is not pending.             | 400         | Verify transaction status and workflow.        |
| SSRVVRF00302 | Transaction has expired.                       | 400         | Create new transaction or extend expiry.      |
| SSRVVRF00303 | Subtransaction not found.                      | 400         | Check subtransaction ID and existence.         |
| SSRVVRF00304 | Subtransaction status is invalid.              | 400         | Verify subtransaction status and workflow.     |

<br>

## 1-4. Authentication and Cryptography Errors (400-499)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00400 | Invalid AuthType: Type mismatch.               | 400         | Check authentication type and configuration.   |
| SSRVVRF00401 | Crypto error occurred.                         | 500         | Check cryptographic operations and parameters. |
| SSRVVRF00402 | Invalid nonce.                                 | 400         | Verify nonce generation and validity.          |
| SSRVVRF00403 | Invalid proof purpose.                         | 400         | Check proof purpose format and validity.       |
| SSRVVRF00404 | Failed to merge nonce.                         | 500         | Check nonce merging algorithm and process.     |
| SSRVVRF00405 | Failed to generate shared secret.              | 500         | Verify shared secret generation process.       |
| SSRVVRF00406 | Failed to merge shared secret and nonce.       | 500         | Check merging algorithm for secret and nonce.  |
| SSRVVRF00407 | Failed to generate nonce.                      | 500         | Verify nonce generation algorithm.             |
| SSRVVRF00408 | Failed to generate key pair.                   | 500         | Check key generation algorithm and parameters. |
| SSRVVRF00409 | Failed to decrypt data.                        | 500         | Verify decryption process and keys.            |
| SSRVVRF00410 | Invalid ECC curve type.                        | 500         | Check ECC curve configuration and support.     |
| SSRVVRF00411 | Invalid symmetric cipher type.                 | 500         | Verify symmetric cipher type and configuration. |
| SSRVVRF00412 | Invalid symmetric padding type.                | 500         | Check symmetric padding type and configuration. |

<br>

## 1-5. Wallet and Key Management Errors (500-599)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00500 | Failed to connect to wallet.                   | 500         | Check wallet connection settings and network.  |
| SSRVVRF00501 | Failed to generate wallet signature.           | 500         | Verify wallet signature generation process.    |
| SSRVVRF00502 | Failed to compress public key.                 | 500         | Check public key compression algorithm.        |
| SSRVVRF00503 | Failed to get File Wallet Manager.             | 500         | Check File Wallet Manager initialization.      |
| SSRVVRF00504 | Failed to create wallet: wallet already exists. | 500        | Use existing wallet or choose different name.  |
| SSRVVRF00505 | Failed to create wallet: invalid wallet file path. | 500     | Verify wallet file path and permissions.       |
| SSRVVRF00506 | Failed to generate keys: key already exists.   | 500         | Use existing key or choose different identifier. |
| SSRVVRF00507 | Failed to load key element.                    | 500         | Check key element format and accessibility.    |
| SSRVVRF00508 | Failed to create wallet.                       | 500         | Check wallet creation process and requirements. |

<br>

## 1-6. DID Related Errors (600-699)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00600 | Failed to retrieve DID Document.               | 500         | Check DID document accessibility and format.   |
| SSRVVRF00601 | Failed to find DID document.                   | 500         | Verify DID document existence and location.    |
| SSRVVRF00602 | Invalid DID Document.                          | 400         | Check DID document format and structure.       |
| SSRVVRF00603 | Verifier is already registered.                | 400         | Use existing verifier or update registration.  |
| SSRVVRF00604 | Failed to register Verifier DID Document: document is already registered. | 400 | Use existing document or update registration. |
| SSRVVRF00605 | Failed to generate DID document.               | 500         | Check DID document generation process.         |
| SSRVVRF00606 | Failed to register Verifier DID Document.      | 500         | Verify registration process and requirements.  |
| SSRVVRF00607 | Failed to find Verifier DID Document: no registration request has been made. | 400 | Complete registration process first. |
| SSRVVRF00608 | Failed to register Verifier DID Document: document is already requested. | 400 | Wait for existing request to complete. |
| SSRVVRF00609 | Failed to process certificate VC: invalid JSON format. | 500 | Check certificate VC JSON format and structure. |

<br>

## 1-7. E2E Related Errors (700-799)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00700 | E2E is not found.                              | 400         | Check E2E configuration and existence.         |
| SSRVVRF00701 | E2E is invalid.                                | 400         | Verify E2E format and validity.                |
| SSRVVRF00702 | Failed to generate key.                        | 500         | Check key generation process and parameters.   |

<br>

## 1-8. Certificate VC Errors (800-899)

| Error Code   | Error Message                  | HTTP Status | Action Required                                |
|--------------|--------------------------------|-------------|------------------------------------------------|
| SSRVVRF00800 | Certificate VC data not found. | 500         | Check certificate VC data existence and storage. |
| SSRVVRF00801 | VC Status is not valid         | 400         | Status of the issued VC is invalid. Please check the VC status |

<br>

## 1-9. API Process Errors (900-999)

| Error Code   | Error Message                                           | HTTP Status | Action Required                                |
|--------------|----------------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00900 | Failed to process the 'request-offer-qr' API request.  | 500         | Check request-offer-qr API parameters and process. |
| SSRVVRF00901 | Failed to process the 'confirm-verify' API request.    | 500         | Verify confirm-verify API request format.     |
| SSRVVRF00902 | Failed to process the 'request-profile' API request.   | 500         | Check request-profile API parameters.         |
| SSRVVRF00903 | Failed to process the 'request-verify' API request.    | 500         | Verify request-verify API format and data.    |
| SSRVVRF00904 | Failed to process the 'get-certificate-vc' API request. | 500        | Check get-certificate-vc API process.         |
| SSRVVRF00905 | Failed to process the 'issue-certificate-vc' API request. | 500      | Verify issue-certificate-vc API parameters.   |
| SSRVVRF00906 | Failed to process the 'request-proof-request-profile' API request. | 500 | Check request-proof-request-profile API format. |

<br>

## 1-10. Verifier Errors (1000-1099)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF01000 | Failed to find verifier: verifier is not registered. | 500      | Register verifier before using.               |

<br>

## 1-11. Admin Errors (1100-1199)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF01100 | Failed to find admin: admin is not registered. | 400        | Register admin account before using.          |
| SSRVVRF01101 | Failed to register admin: admin is already registered. | 400    | Use existing admin account or update.         |
| SSRVVRF01102 | Failed to communicate with tas: unknown error occurred. | 500   | Check TAS connection and configuration.        |
| SSRVVRF01103 | Failed to process response: received unknown data from the tas. | 500 | Verify TAS response format and compatibility. |

<br>

## 1-12. ZKP Errors (1200-1299)

| Error Code   | Error Message                                           | HTTP Status | Action Required                                |
|--------------|----------------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF01201 | Failed to find Proof request profile : request proof profile not found | 400 | Check proof request profile existence and configuration. |
| SSRVVRF01202 | Failed to find ZKP policy profile : request proof profile not found | 400 | Verify ZKP policy profile configuration.       |
| SSRVVRF01203 | Failed to parse proof request profile : request proof profile parse error | 500 | Check proof request profile format and structure. |
| SSRVVRF01204 | Failed to find ZKP proof request : request proof profile not found | 400 | Verify ZKP proof request data and format.      |
| SSRVVRF01205 | Failed to verify proof : proof verify failed           | 500         | Check ZKP proof validity and verification process. |
| SSRVVRF01206 | Failed to retrieve ZKP credential on the blockchain.   | 500         | Check blockchain connection and ZKP credential data. |
| SSRVVRF01207 | Failed to retrieve ZKP credential definition on the blockchain. | 500 | Verify blockchain connection and credential definition. |
| SSRVVRF01208 | Failed to find credential schema : credential schema not found | 400 | Check credential schema existence and format.   |
| SSRVVRF01209 | Failed to find proof request : proof request not found | 400         | Verify proof request data and configuration.    |

<br>

## 1-13. Protocol and OID4VP Errors (1300-1399)

| Error Code   | Error Message | HTTP Status | Action Required |
|--------------|---------------|-------------|-----------------|
| SSRVVRF01300 | Unsupported protocol type. | 400 | Use a supported protocol type (DID_VP or OID4VP). |
| SSRVVRF01301 | Protocol handler not found for the given protocol type. | 500 | Check the protocol type configured in the policy. |
| SSRVVRF01302 | Failed to initiate verification. | 500 | Check the policy and OID4VP configuration, then check the server logs. |
| SSRVVRF01303 | DCQL scope mapping not found. | 400 | Register an enabled DCQL scope mapping for the policy scope in the admin console. |
| SSRVVRF01304 | OID4VP configuration not found. | 500 | Check that the OID4VP configuration is registered. |
| SSRVVRF01305 | Failed to initiate OID4VP verification. | 500 | Check the OID4VP configuration and the DCQL query of the scope mapping. |
| SSRVVRF01306 | OID4VP session mapping not found. | 400 | Check that the transaction or state is valid and has not expired. |
| SSRVVRF01307 | Failed to retrieve OID4VP authorization request. | 500 | Check that the request ID is valid and has not expired. |
| SSRVVRF01308 | Failed to process OID4VP response. | 500 | Check the submitted vp_token and the server logs. |
| SSRVVRF01309 | Failed to generate OID4VP encryption key pair. | 500 | Check the server cryptography environment and the server logs. |
| SSRVVRF01310 | Failed to load OID4VP encryption private key. | 500 | Check vpTokenEncryptionKey in the OID4VP configuration. Sessions created before the key was changed cannot be decrypted. |
| SSRVVRF01311 | Failed to parse JWE header. | 400 | Check that the response is a valid compact JWE with a kid header. |
| SSRVVRF01312 | Failed to decrypt OID4VP JWE response. | 400 | Check that the wallet encrypted the response with the key in the request's client_metadata. |
| SSRVVRF01313 | OID4VP encryption alg/enc is fixed and cannot be changed to a different value. | 400 | Keep the encryption values as alg ECDH-ES and enc A256GCM when saving the OID4VP configuration. |
| SSRVVRF01314 | This session requires an encrypted (direct_post.jwt) response; plaintext submission is not accepted. | 400 | Submit the response encrypted as a JWE (direct_post.jwt). |
| SSRVVRF01315 | OID4VP responseMode is fixed to direct_post.jwt and cannot be changed to a different value. | 400 | Keep responseMode as direct_post.jwt when saving the OID4VP configuration. |
| SSRVVRF01316 | This verifier only accepts DID-based (kid) mso_mdoc; x5chain-based mdoc is not supported. | 400 | Submit an mso_mdoc credential signed with a DID key (kid). |

<br>

## 1-14. Status List Errors (1400-1499)

| Error Code   | Error Message | HTTP Status | Action Required |
|--------------|---------------|-------------|-----------------|
| SSRVVRF01401 | Failed to fetch status list token. | 500 | Check that the status list URI (http or https) is reachable. For a private-network or loopback host, add it to verifier.status-list.allowed-private-hosts. |
| SSRVVRF01402 | Status list token validation failed. | 400 | Check the status list token issued by the issuer (signature, format, and expiry). |
| SSRVVRF01403 | Credential has been revoked. | 400 | The credential is revoked. Request a new credential from the issuer. |
| SSRVVRF01404 | Credential is suspended. | 400 | The credential is suspended. Contact the issuer. |
| SSRVVRF01405 | Status list index is out of bounds. | 400 | Check the status list index in the credential. |
| SSRVVRF01406 | Credential status is reserved and cannot be treated as valid. | 400 | Check the credential status with the issuer. |

<br>

## 1-15. Server Configuration Errors (21500-21599)

| Error Code   | Error Message | HTTP Status | Action Required |
|--------------|---------------|-------------|-----------------|
| SSRVVRF21501 | Server configuration key not found. | 400 | Register the key (for example, API_KEY_LSS) in Server Configuration of the admin console. |
