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

Verifier API
==

- Date: 2024-08-19
- Version: v1.0.0

## Revision History

| Version     | Date       | Changes                                                          |
| ----------- | ---------- | ---------------------------------------------------------------- |
| 1.0.0       | 2024-09-03 | Initial release                                                  |
| 1.0.1 (dev) | 2024-03-31 | [4.1 Request Offer(QR)] Request data modified (added policyId, removed device, service, mode) |
| 1.0.2 (dev) | 2024-XX-XX | [5. P311 - ZKP Proof Submission Protocol] Added                 |

<!-- TOC tocDepth:2..3 chapterDepth:2..6 -->
  
Table of Contents
---
  - [1. Overview](#1-overview)
  - [2. Terminology](#2-terminology)
  - [3. API List](#3-api-list)
    - [3.1. Sequential API](#31-sequential-api)
    - [3.2. Single Call API](#32-single-call-api)
  - [4. P310 - VP Submission Protocol](#4-p310---vp-submission-protocol)
    - [4.1. Request Offer (QR)](#41-request-offer-qr)
    - [4.2. Request Profile](#42-request-profile)
    - [4.3. Request Verify](#43-request-verify)
    - [4.4. Confirm Verify](#44-confirm-verify)
  - [5. P311 - ZKP Proof Submission Protocol](#5-p311---zkp-proof-submission-protocol)
    - [5.1. Request Proof Request Profile](#51-request-proof-request-profile)
    - [5.2. Request Verify Proof](#52-request-verify-proof)
  - [6. Single Call API](#6-single-call-api)
    - [6.1. Issue Certificate VC](#61-issue-certificate-vc)
    - [6.2. Get Certificate Vc](#62-get-certificate-vc)


## 1. Overview

This document defines the APIs provided by the Verifier Service.

![Workflow](images/workflow_verifier.svg)

- The above diagram shows the protocols and APIs provided by or called by the Verifier Service. For readability, only Standard APIs are displayed.
- Each term is explained in Chapter 2, and API lists and call examples can be found from Chapter 3 onwards.

<div style="page-break-after: always; margin-top: 50px;"></div>

## 2. Terminology
- Protocol
  - A set of `Sequential APIs` that must be called in a predetermined order to perform a specific function. The API call sequence must be strictly followed, as incorrect ordering may lead to unexpected results.
  - Protocols start with P and consist of 3 digits.
    - Example: P310 - VP Submission Protocol, P311 - ZKP Proof Submission Protocol
- Sequential API
  - A series of APIs that are called in a predetermined order to perform a specific function (protocol). Each API must be called sequentially, and incorrect ordering may prevent proper operation.
  - However, some protocols may have APIs with the same calling sequence, in which case one API can be selected and called.
- Single Call API
  - An API that can be called independently regardless of order, like typical REST APIs.
- Standard API
  - APIs clearly defined in the API documentation that must be provided consistently across all implementations. Standard APIs ensure interoperability between systems and must operate according to predefined specifications.
- Non-Standard API
  - APIs that can be defined or customized differently according to the needs of each implementation. The non-standard APIs provided in this document are just one example and may be implemented differently for each implementation. In this case, separate documentation for each implementation is required.
  - For example, VP submission result verification may be implemented differently depending on the system, and non-standard APIs like `confirm-verify` can be redefined as needed by each implementation.

<div style="page-break-after: always; margin-top: 50px;"></div>

## 3. API List

### 3.1. Sequential API

#### 3.1.1. P310 - VP Submission Protocol
| Seq | API                | URL                      | Description            | Standard API |
| --- | ------------------ | ------------------------ | ---------------------- | ------------ |
| 1   | `request-offer-qr` | /api/v1/request-offer-qr | VP submission Offer request (QR) | N |
| 2   | `request-profile`  | /api/v1/request-profile  | Submission Profile request | Y |
| 3   | `request-verify`   | /api/v1/request-verify   | VP submission          | Y |
| 4   | `confirm-verify`   | /api/v1/confirm-verify   | VP submission result confirmation | N |

#### 3.1.2. P311 - ZKP Proof Submission Protocol
| Seq | API                           | URL                                   | Description              | Standard API |
| --- | ----------------------------- | ------------------------------------- | ------------------------ | ------------ |
| 1   | `request-proof-request-profile` | /api/v1/request-proof-request-profile | ZKP Proof request Profile request | Y |
| 2   | `request-verify-proof`        | /api/v1/request-verify-proof          | ZKP Proof submission     | Y |

<div style="page-break-after: always; margin-top: 40px;"></div>

### 3.2. Single Call API

| API                    | URL                    | Description            | Standard API |
| ---------------------- | ---------------------- | ---------------------- | ------------ |
| `issue-certificate-vc` | /api/v1/certificate-vc | Entity registration request | N |
| `get-certificate-vc`   | /api/v1/certificate-vc | Certificate inquiry    | N |

<div style="page-break-after: always; margin-top: 50px;"></div>

## 4. P310 - VP Submission Protocol

| Seq. | API              | Description            | Standard API |
| :--: | ---------------- | ---------------------- | ------------ |
|  1   | request-offer-qr | VP submission Offer request (QR) | N |
|  2   | request-profile  | Submission Profile request | Y |
|  3   | request-verify   | VP submission          | Y |
|  4   | confirm-verify   | VP submission result confirmation | N |

- In the VP submission protocol, API callers vary depending on the situation, making it difficult to always attach `txId` to messages.
- Therefore, the Verifier Service can generate and respond with `txId`, but does not mandate its inclusion in request messages.

### 4.1. Request Offer (QR)

Request VP submission session information.

The Verifier already has information about the VP to be submitted, so the Verifier can provide submission session information to the responding device. This submission session information is called "Verify Offer".

This API transmits the Payload of the Verify Offer, and the responding device can convert this Payload to QR and display it on screen.

| Item          | Description                         | Remarks |
| ------------- | ----------------------------------- | ------- |
| Method        | `POST`                              |         |
| Path          | `/api/v1/request-offer-qr` |         |
| Authorization | -                                   |         |

#### 4.1.1. Request

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ HTTP Body**

```c#
def object RequestOfferQr: "Request Offer QR request"
{    
    - messageId "id": "message id"       
    + itemName "policyId": "VP Policy identifier"
}
```

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.1.2. Response

**■ Process**
1. Retrieve VP Policy by policyId
1. Generate offerId
1. Generate offer expiration time (validUntil)
1. Create Verify Offer Payload
1. Save mapped to offerId
    - mode, device, service, policyId, validUntil

**■ Status 200 - Success**

```c#
def object _RequestOfferQr: "Request Offer QR response"
{    
    + uuid "txId"  : "transaction id"
    + VerifyOfferPayload "payload" : "submission Offer Payload" // Refer to data specification
}
```

**■ Status 400 - Client error**

| Code         | Description              |
| ------------ | ------------------------ |
| SSRVVRF00201 | VP_POLICY is not found. |

**■ Status 500 - Server error**

| Code         | Description                                        |
| ------------ | -------------------------------------------------- |
| SSRVVRF00900 | Failed to process 'request-offer-qr' API request. |

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.1.3. Example

**■ Request**

```shell
curl -v -X POST "http://${Host}:${Port}/verifier/api/v1/request-offer-qr" \
-H "Content-Type: application/json;charset=utf-8" \
-d @"data.json"
```

```json
{
  "id":"202303241738241234561234ABCD",
  "policyId": "f1a2b3c4-d5e6-7890-1234-56789abcdef0"
}
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8

{    
  "txId": "70e22f4e-ca60-48d3-88ee-a43d42ad3313",
  "payload": {
    "offerId": "3bf6a1a0-beca-4450-a01e-4e2166819504",
    "type": "VerifyOffer",
    "mode": "Direct",
    "device": "WEB",
    "service": "login",
    "endpoints": [
      "http://127.0.0.130:8092/verifier/api/v1/request-verify"
    ],
    "validUntil": "2024-01-01T08:44:58.048519445Z",
    "locked": false
  }
}
```

<div style="page-break-after: always; margin-top: 40px;"></div>

### 4.2. Request Profile

Submit `offerId` to request VerifyProfile.

| Item          | Description               | Remarks |
| ------------- | ------------------------- | ------- |
| Method        | `POST`                    |         |
| Path          | `/api/v1/request-profile` |         |
| Authorization | -                         |         |

#### 4.2.1. Request

**■ Headers**

| Header           | Value                            | Remarks |
| ---------------- | -------------------------------- | ------- |
| + `Content-Type` | `application/json;charset=utf-8` |         |

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ Body**

```c#
def object M310_RequestProfile: "Request Profile request"
{
    //--- Common Part ---
    + messageId "id"  : "message id"
    - uuid      "txId": "transaction id"
    //--- Data Part ---
    + uuid "offerId" : "verify offer id"
}
```

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.2.2. Response

**■ Process**

1. (If needed) Verify transaction code
1. Validate `offerId`
1. `profile` = Generate VerifyProfile with signature attached

**■ Status 200 - Success**

```c#
def object _M310_RequestProfile: "Request Profile response"
{    
    //--- Common Part ---
    + uuid "txId": "transaction id"

    //--- Data Part ---
    + VerifyProfile "profile": "verify profile"
}
```

**■ Status 400 - Client error**

|     Code     | Description                         |
| :----------: | ----------------------------------- |
| SSRVVRF00200 | VP_OFFER is not found.              |
| SSRVVRF00201 | VP_POLICY is not found.             |
| SSRVVRF00300 | Transaction not found.              |
| SSRVVRF00301 | Transaction status is not pending.  |
| SSRVVRF00302 | Transaction has expired.            |
| SSRVVRF00303 | Subtransaction not found.           |

**■ Status 500 - Server error**

|     Code     | Description                                    |
| :----------: | ---------------------------------------------- |
| SSRVVRF00401 | Crypto error occurred.                         |
| SSRVVRF00408 | Failed to generate key pair.                  |
| SSRVVRF00502 | Failed to compress public key.                |
| SSRVVRF00600 | Failed to retrieve DID Document.              |
| SSRVVRF00902 | Failed to process 'request-profile' API request. |

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.2.3. Example

**■ Request**

```shell
curl -v -X POST "http://${Host}:${Port}/verifier/api/v1/request-profile" \
-H "Content-Type: application/json;charset=utf-8" \
-d @"data.json"
```

```json
//data.json
{
  "txId": "b38aa1e3-48fa-4f5f-be60-05042d9ec660",
  "offerId": "3bf6a1a0-beca-4450-a01e-4e2166819504",
  "id": "202303241738241234561234ABCD"
}
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8
{
  "txId": "b38aa1e3-48fa-4f5f-be60-05042d9ec660",
  "profile": {
    "id": "b8302842-0d9b-4f95-9da8-5ae3bbf8dd69",
    "type": "VerifyProfile",
    "title": "OpenDID Login VP Profile",
    "description": "Profile for VP required for OpenDID login submission.",
    "encoding": "UTF-8",
    "language": "ko",
    "profile": {
      "verifier": {
        "did": "did:omn:verifier",
        "certVcRef": "http://127.0.0.130:8092/verifier/api/v1/certificate-vc",
        "name": "verifier",
        "description": "verifier",
        "ref": "http://127.0.0.130:8092/swagger-ui/index.html#/"
      },
      "filter": {
        "credentialSchemas": [
          {
            "id": "http://127.0.0.130:8091/issuer/api/v1/vc/vcschema?name=mdl",
            "type": "OsdSchemaCredential",
            "value": "VerifiableProfile",
            "presentAll": false,
            "displayClaims": [
              "testId.aa"
            ],
            "requiredClaims": [
              "org.iso.18013.5.birth_date",
              "org.iso.18013.5.family_name",
              "org.iso.18013.5.given_name"
            ],
            "allowedIssuers": [
              "did:omn:issuer"
            ]
          }
        ]
      },
      "process": {
        "endpoints": [
          "http://127.0.0.130:8092/verifier/api/v1/request-verify"
        ],
        "reqE2e": {
          "nonce": "mLXd8kMD3pb4WRAnchWudXA",
          "curve": "Secp256r1",
          "publicKey": "z26VWT8GTUxNdRAXUThK4rRPzAeWsXf7....",
          "cipher": "AES-256-CBC",
          "padding": "PKCS5"
        },
        "verifierNonce": "mLXd8kMD3pb4WRAnchWudXA",
        "authType": 6
      }
    },
    "proof": {
      "type": "Secp256r1Signature2018",
      "created": "2024-10-25T17:42:09.060550Z",
      "verificationMethod": "did:omn:verifier?versionId=1#assert",
      "proofPurpose": "assertionMethod",
      "proofValue": "z3m8feWmLrhsYkym2PcfmpHe1sRkL5BQba4d...."
    }
  }
}
```

<div style="page-break-after: always; margin-top: 40px;"></div>

### 4.3. Request Verify

Receive encrypted VP, decrypt, verify, and provide service.

| Item          | Description              | Remarks |
| ------------- | ------------------------ | ------- |
| Method        | `POST`                   |         |
| Path          | `/api/v1/request-verify` |         |
| Authorization | -                        |         |

#### 4.3.1. Request

**■ Headers**

| Header           | Value                            | Remarks |
| ---------------- | -------------------------------- | ------- |
| + `Content-Type` | `application/json;charset=utf-8` |         |

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ Body**

```c#
def object M310_RequestVerify: "Request Verify request"
{
    //--- Common Part ---
    + messageId "id"  : "message id"
    - uuid      "txId": "transaction id"

    //--- Data Part ---
    + AccE2e    "accE2e": "E2E acceptance information"
    + multibase "encVp" : "multibase(enc((Vp)vp))"
}
```

- `~/accE2e`: E2E acceptance information corresponding to `VerifyProfile:~/profile/process/reqE2e`
- `~/encVp`: VP encrypted with E2E key

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.3.2. Response

Generate E2E encryption key using E2E acceptance information and decrypt VP.
Must verify that verifierNonce in VP matches what was provided by the verification provider.

**■ Process**

1. Verify transaction code
1. (If exists) Verify `accE2e.proof` signature
1. Perform E2E ECDH
    - `e2eKey` = Generate encryption key
    - `iv` = accE2e.iv
1. Decrypt `encVp`
    - vp = dec(debase(encVp), e2eKey, iv, padding)
1. Verify `vp` content
    - Check if `profile/process/verifierNonce` matches
    - Verify issuer signature
1. If non-repudiation is required, encrypt and store `vp` (encryption method is out of scope)
1. Provide service (according to verification provider's service)

**■ Status 200 - Success**

```c#
def object _M310_RequestVerify: "Request Verify response"
{    
    //--- Common Part ---
    + uuid "txId": "transaction id"
}
```

**■ Status 400 - Client error**

|     Code     | Description                                 |
| :----------: | ------------------------------------------- |
| SSRVVRF00107 | Failed to decode data: incorrect encoding  |
| SSRVVRF00300 | Transaction not found.                     |
| SSRVVRF00301 | Transaction status is invalid.             |
| SSRVVRF00302 | Transaction has expired.                   |
| SSRVVRF00303 | Subtransaction not found.                  |
| SSRVVRF00304 | Subtransaction status is invalid.          |
| SSRVVRF00402 | Invalid nonce.                             |
| SSRVVRF00403 | Invalid ProofPurpose.                      |

**■ Status 500 - Server error**

|     Code     | Description                                    |
| :----------: | ---------------------------------------------- |
| SSRVVRF00101 | Failed to parse JSON.                          |
| SSRVVRF00103 | Failed to retrieve DID document on the blockchain. |
| SSRVVRF00104 | Failed to verify signature.                    |
| SSRVVRF00105 | Failed to generate hash.                       |
| SSRVVRF00106 | Failed to encode data.                         |
| SSRVVRF00202 | VP verification failed.                        |
| SSRVVRF00204 | Failed to parse VP profile.                    |
| SSRVVRF00400 | Invalid AuthType: Type mismatch.               |
| SSRVVRF00401 | Crypto error occurred.                         |
| SSRVVRF00601 | Failed to find DID document.                   |
| SSRVVRF00700 | E2E is not found.                              |
| SSRVVRF00701 | E2E is invalid.                                |
| SSRVVRF00903 | Failed to process 'request-verify' API request. |

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.3.3. Example

**■ Request**

```shell
curl -v -X POST "http://${Host}:${Port}/verifier/api/v1/request-verify" \
-H "Content-Type: application/json;charset=utf-8" \
-d @"data.json"
```

```json
{
  "accE2e": {
    "iv": "z2SXXDRzxTyKt8ua7Y96GPK",
    "proof": {
      "created": "2024-01-01T06:32:33Z",
      "proofPurpose": "keyAgreement",
      "proofValue": "z3odiy3M5SJMGzXHXyQZKEFMXaqPGcBVyTPYKURKA....",//encodeData
      "type": "Secp256r1Signature2018",
      "verificationMethod": "did:omn:iuBdTVmXCwntmEtjPR5eYZRLM5W?versionId=1#keyagree"
    },
    "publicKey": "z21hpr3CASVzgJ2azYZtxzedTmKyKLRoaNGiK9vGfLPCQJ"
  },
  "encVp": "zRedoZXk23311kmCg4yKEwLBAxz2RG4P6hopt9Dn4CS....",//encodeData
  "id": "202410241532334100006CE70B2E",
  "txId": "a3333175-a799-4ae3-8f9c-5b9f4c0f579f"
}
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8

{
    "txId":"a3333175-a799-4ae3-8f9c-5b9f4c0f579f"
}
```

<div style="page-break-after: always; margin-top: 40px;"></div>

### 4.4. Confirm Verify

Inquire VP submission result.

Also extract and deliver claim information from the VP submitted by the authorized app. Claims may contain personal information, but encryption is not processed for testing convenience.

| Item          | Description              | Remarks |
| ------------- | ------------------------ | ------- |
| Method        | `POST`                   |         |
| Path          | `/api/v1/confirm-verify` |         |
| Authorization | -                        |         |

#### 4.4.1. Request

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ HTTP Body**

```c#
def object ConfirmVerify: "Confirm Verify request"
{    
    + uuid  "offerId": "submission offer id" 
}
```

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.4.2. Response

**■ Process**
1. Retrieve VP submission information by offerId
1. Extract claim information from VP submission information


**■ Status 200 - Success**

```c#
def object _ConfirmVerify: "Confirm Verify response"
{    
    + bool "result": "VP submission result"
    - array(Claim) "claims" : "submitted claim information" // Refer to data specification
}
```

**■ Status 400 - Client error**

| Code         | Description           |
| ------------ | --------------------- |
| SSRVVRF00300 | Transaction not found.|
| SSRVVRF00200 | VP_OFFER is not found.|

**■ Status 500 - Server error**

| Code         | Description                                    |
| ------------ | ---------------------------------------------- |
| SSRVVRF00901 | Failed to process 'confirm-verify' API request. |

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 4.4.3. Example

**■ Request**

```shell
curl -v -X POST "http://${Host}:${Port}/verifier/api/v1/confirm-verify" \
-H "Content-Type: application/json;charset=utf-8" \
-d @"data.json"
```

```json
{    
  "offerId":"5e6af61a-96f3-42db-9359-d4299f6e7a8f"
}
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8

{
    "claims": [
        {
        "code": "org.iso.18013.5.family_name",
        "caption": "Family Name",
        "value": "Kim",
        "type": "text",
        "format": "plain",
        "hideValue": false
        },
        {
        "code": "org.iso.18013.5.given_name",
        "caption": "Given Name",
        "value": "Raon",
        "type": "text",
        "format": "plain",
        "hideValue": false
        },
        {
        "code": "org.iso.18013.5.birth_date",
        "caption": "Birth date",
        "value": "2024-01-01",
        "type": "text",
        "format": "plain",
        "hideValue": false
        }
    ],
    "result": true
}
```

<div style="page-break-after: always; margin-top: 50px;"></div>

## 5. P311 - ZKP Proof Submission Protocol

| Seq. | API                          | Description              | Standard API |
| :--: | ---------------------------- | ------------------------ | ------------ |
|  1   | request-proof-request-profile | ZKP Proof request Profile request | Y |
|  2   | request-verify-proof         | ZKP Proof submission     | Y |

- The ZKP Proof submission protocol is a VP submission method utilizing Zero-Knowledge Proof.
- Unlike traditional VP submission, it can prove satisfaction of specific conditions without exposing actual claim values.

### 5.1. Request Proof Request Profile

Request Profile information required for ZKP Proof request.

The Verifier provides a ProofRequestProfile containing proof requirements and parameters necessary for ZKP verification.

| Item          | Description                               | Remarks |
| ------------- | ----------------------------------------- | ------- |
| Method        | `POST`                                    |         |
| Path          | `/api/v1/request-proof-request-profile`   |         |
| Authorization | -                                         |         |

#### 5.1.1. Request

**■ Headers**

| Header           | Value                            | Remarks |
| ---------------- | -------------------------------- | ------- |
| + `Content-Type` | `application/json;charset=utf-8` |         |

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ Body**

```c#
def object M311_RequestProofRequestProfile: "Request Proof Request Profile request"
{
    //--- Common Part ---
    + messageId "id"  : "message id"
    - uuid      "txId": "transaction id"
    //--- Data Part ---
    + uuid "offerId" : "verify offer id"    
}
```

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 5.1.2. Response

**■ Process**

1. (If needed) Verify transaction code
1. Validate `offerId`
1. Check `proofType` through `offerId` and generate ProofRequestProfile with signature attached

**■ Status 200 - Success**

```c#
def object _M311_RequestProofRequestProfile: "Request Proof Request Profile response"
{    
    //--- Common Part ---
    + uuid "txId": "transaction id"

    //--- Data Part ---
    + ProofRequestProfile "proofRequestProfile": "ZKP proof request profile"
    //Need to check data specification
}
```

**■ Status 400 - Client error**

|     Code     | Description                         |
| :----------: | ----------------------------------- |
| SSRVVRF00200 | VP_OFFER is not found.              |
| SSRVVRF00201 | VP_POLICY is not found.             |
| SSRVVRF00300 | Transaction not found.              |
| SSRVVRF00301 | Transaction status is not pending.  |
| SSRVVRF00302 | Transaction has expired.            |
| SSRVVRF00303 | Subtransaction not found.           |
| SSRVVRF00310 | Unsupported proof type.             |

**■ Status 500 - Server error**

|     Code     | Description                                                       |
| :----------: | ----------------------------------------------------------------- |
| SSRVVRF00401 | Crypto error occurred.                                            |
| SSRVVRF00408 | Failed to generate key pair.                                     |
| SSRVVRF00502 | Failed to compress public key.                                   |
| SSRVVRF00600 | Failed to retrieve DID Document.                                 |
| SSRVVRF00906 | Failed to process 'request-proof-request-profile' API request.   |

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 5.1.3. Example

**■ Request**

```shell
curl -v -X POST "http://${Host}:${Port}/verifier/api/v1/request-proof-request-profile" \
-H "Content-Type: application/json;charset=utf-8" \
-d @"data.json"
```

```json
//data.json
{
  "txId": "b38aa1e3-48fa-4f5f-be60-05042d9ec660",
  "offerId": "3bf6a1a0-beca-4450-a01e-4e2166819504",
  "id": "202303241738241234561234ABCD",
}
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8
{
  "txId": "b38aa1e3-48fa-4f5f-be60-05042d9ec660",
  "proofRequestProfile": {
    "id": "c8302842-0d9b-4f95-9da8-5ae3bbf8dd70",
    "type": "ProofRequestProfile",
    "title": "연령 범위 증명 요청 프로파일",
    "description": "만 18세 이상임을 ZKP로 증명하기 위한 프로파일입니다.",
    "encoding": "UTF-8",
    "language": "ko",
    "profile": {
      "verifier": {
        "did": "did:omn:verifier",
        "certVcRef": "http://127.0.0.130:8092/verifier/api/v1/certificate-vc",
        "name": "verifier",
        "description": "verifier",
        "ref": "http://127.0.0.130:8092/swagger-ui/index.html#/"
      },
      "proofRequest": {
        "name": "mdl",
        "nonce": "1068995366822249097155600",
        "requestedAttributes": {
          "attributeReferent1": {
            "name": "zkpsex",
            "restrictions": [
              {
                "credDefId": "did:omn:NcYxiDXkpYi6ov5FcYDi1e:3:CL:did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:schemaname:1.0:Tag1"
              }
            ]
          }
        },
        "requestedPredicates": {
          "predicateReferent1": {
            "name": "zkpbirth",
            "pType": "LE",
            "pValue": 20200103,
            "restrictions": [
              {
                "credDefId": "did:omn:NcYxiDXkpYi6ov5FcYDi1e:3:CL:did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:schemaname:1.0:Tag1"
              }
            ]
          }
        }
      },
        "reqE2e": {
          "nonce": "mLXd8kMD3pb4WRAnchWudXA",
          "curve": "Secp256r1",
          "publicKey": "z26VWT8GTUxNdRAXUThK4rRPzAeWsXf7....",
          "cipher": "AES-256-CBC",
          "padding": "PKCS5"
        },
    },
    "proof": {
      "type": "Secp256r1Signature2018",
      "created": "2024-10-25T17:42:09.060550Z",
      "verificationMethod": "did:omn:verifier?versionId=1#assert",
      "proofPurpose": "assertionMethod",
      "proofValue": "z3m8feWmLrhsYkym2PcfmpHe1sRkL5BQba4e...."
    }
  }
}
```

<div style="page-break-after: always; margin-top: 40px;"></div>


<div style="page-break-after: always; margin-top: 40px;"></div>

### 5.2. Request Verify Proof

Receive and verify ZKP Proof.

| Item          | Description                      | Remarks |
| ------------- | -------------------------------- | ------- |
| Method        | `POST`                           |         |
| Path          | `/api/v1/request-verify-proof`   |         |
| Authorization | -                                |         |

#### 5.2.1. Request

**■ Headers**

| Header           | Value                            | Remarks |
| ---------------- | -------------------------------- | ------- |
| + `Content-Type` | `application/json;charset=utf-8` |         |

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ Body**

```c#
def object M311_RequestVerifyProof: "Request Verify Proof request"
{
    //--- Common Part ---
    + messageId "id"  : "message id"
    - uuid      "txId": "transaction id"

    //--- Data Part ---
    + AccE2e    "accE2e": "E2E acceptance information"
    + multibase "encProof" : "multibase(enc((Proof)proof))"    
    + BigInterger  "nonce": "Proof Nonce"   
}
```

- `~/accE2e`: E2E acceptance information corresponding to reqE2e
- `~/encProof`: Proof encrypted with E2E key: public input values used in proof
- `~/nonce`: Nonce provided in ProofRequestProfile

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 5.2.2. Response

Verify ZKP Proof and return proof result.

**■ Process**

1. Verify transaction code
1. Check if `nonce` matches
1. Verify ZKP proof
    - Check circuit parameters
    - Verify public input values
    - Validate proof
1. Save proof result (submission status only)
1. Provide service (according to verification provider's service)

**■ Status 200 - Success**

```c#
def object _M311_RequestVerifyProof: "Request Verify Proof response"
{    
    //--- Common Part ---
    + uuid "txId": "transaction id"
    
    //--- Data Part ---
    + bool "verified": "proof verification result"
    - string "proofType": "proof type"
    - object "verificationDetails": "verification details"
}
```

**■ Status 400 - Client error**

|     Code     | Description                                 |
| :----------: | ------------------------------------------- |
| SSRVVRF00300 | Transaction not found.                     |
| SSRVVRF00301 | Transaction status is invalid.             |
| SSRVVRF00302 | Transaction has expired.                   |
| SSRVVRF00303 | Subtransaction not found.                  |
| SSRVVRF00304 | Subtransaction status is invalid.          |
| SSRVVRF00402 | Invalid nonce.                             |
| SSRVVRF00311 | Invalid ZKP proof.                         |
| SSRVVRF00312 | Public input values are invalid.           |

**■ Status 500 - Server error**

|     Code     | Description                                          |
| :----------: | ---------------------------------------------------- |
| SSRVVRF00101 | Failed to parse JSON.                               |
| SSRVVRF00105 | Failed to generate hash.                            |
| SSRVVRF00106 | Failed to encode data.                              |
| SSRVVRF00210 | ZKP proof verification failed.                      |
| SSRVVRF00911 | Failed to process 'request-verify-proof' API request. |


<div style="page-break-after: always; margin-top: 30px;"></div>

#### 5.2.3. Example

**■ Request**

```shell
curl -v -X POST "http://${Host}:${Port}/verifier/api/v1/request-verify-proof" \
-H "Content-Type: application/json;charset=utf-8" \
-d @"data.json"
```

```json
{
  "accE2e": {
    "iv": "zXnp5USMEwh9bkFYosG7jBS",
    "proof": {
      "created": "2025-05-26T10:39:01Z",
      "proofPurpose": "keyAgreement",
      "proofValue": "z3n6CBRfE3hJfmiLtLV9jDsPrGzMpbGy2HyufiTYJ4bs7rWmCytt1ZAtXiX24Veo14uNPLZisoi2bYqZ43BASRqVjb",
      "type": "Secp256r1Signature2018",
      "verificationMethod": "did:omn:AS8rQZnKx7atQH2boY3V8CVvCMo?versionId=1#keyagree"
    },
    "publicKey": "znRs8Um87H9iGoK1iuyums9pndSdLcxdhM8r8TNaDxv1A"
  },
  "encProof": "mnV+UxOXHVMK8fTakXpslk74k66rrJjM+PUvx2BS701f/", //encrypt Proof Data
  "id": "202505261939035230006BFD300B",
  "nonce": "134599285294987166794747",
  "txId": "29db231a-4c25-4e6b-afcc-7a709b3c9638"
}
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8

{
  "txId": "a3333175-a799-4ae3-8f9c-5b9f4c0f579f"
}
```

<div style="page-break-after: always; margin-top: 50px;"></div>

## 6. Single Call API

Single Call APIs are independent APIs that perform a specific function.
Therefore, they are not Sequential APIs (aka protocols) which are groups of APIs that must be called in order, so no protocol number is assigned.
The list of Single Call APIs provided by the Verifier Service is shown in the table below.

| API                    | URL                    | Description            | Standard API |
| ---------------------- | ---------------------- | ---------------------- | ------------ |
| `issue-certificate-vc` | /api/v1/certificate-vc | Entity registration request | N |
| `get-certificate-vc`   | /api/v1/certificate-vc | Certificate inquiry    | N |

■ Authorization

Protocols include APIs that 'verify the caller's call permission' (authorization).
The Single Call APIs in the above list do not define authorization,
but the following approaches are being considered for future addition.

- Option 1) Issue a token that can be used for a certain period after verifying the `AttestedAppInfo` information signed by the authorized app provider
    - Attach TAS-issued token to header when calling single API
    - Separate token management API required
- Option 2) Authorized app provider issues token to authorized app and TAS requests token verification from authorized app provider
    - Attach authorized app provider-issued token to header when calling single API
    - Authorized app provider needs to implement functionality to issue and verify tokens
  
### 6.1. Issue Certificate VC

Request issuance of certificate.

The Verifier's DID Document must already be registered in storage (e.g., blockchain) through the TAS administrator.
This API sequentially calls TAS's P120 protocol APIs to obtain the certificate.

| Item          | Description              | Remarks |
| ------------- | ------------------------ | ------- |
| Method        | `POST`                   |         |
| Path          | `/api/v1/certificate-vc` |         |
| Authorization | -                        |         |

#### 6.1.1. Request

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ HTTP Body**

```c#
def object IssueCertificateVc: "Issue Certificate VC request"
{    
}
```

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 6.1.2. Response

**■ Process**
1. Call TA P120 protocol APIs in sequence
1. Save issued certificate to database

**■ Status 200 - Success**

```c#
def object _IssueCertificateVc: "Issue Certificate VC response"
{    
}
```

**■ Status 400 - Client error**

N/A

**■ Status 500 - Server error**

| Code | Description |
| ---- | ----------- |
| SSRVVRF00401 | Crypto error occurred. |
| SSRVVRF00905 | Failed to process 'issue-certificate-vc' API request. |

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 6.1.3. Example

**■ Request**

```shell
curl -v -X POST "http://${Host}:${Port}/verifier/api/v1/certificate-vc" \
-H "Content-Type: application/json;charset=utf-8" \
-d @"data.json"
```

```json
{
  //no data   
}
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8
{
    //no data
}
```

<div style="page-break-after: always; margin-top: 40px;"></div>

### 6.2. Get Certificate Vc

Retrieve certificate.

| Item          | Description              | Remarks |
| ------------- | ------------------------ | ------- |
| Method        | `GET`                    |         |
| Path          | `/api/v1/certificate-vc` |         |
| Authorization | -                        |         |

#### 6.2.1. Request

**■ HTTP Headers**

| Header           | Value                            | Remarks |
| ---------------- | -------------------------------- | ------- |
| + `Content-Type` | `application/json;charset=utf-8` |         |     

**■ Path Parameters**

N/A

**■ Query Parameters**

N/A

**■ HTTP Body**

N/A

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 6.2.2. Response

**■ Process**
1. Retrieve certificate

**■ Status 200 - Success**

```c#
def object _GetCertificateVc: "Get Certificate VC response"
{
    @spread(Vc)  // Refer to data specification
}
```

**■ Status 400 - Client error**

N/A

**■ Status 500 - Server error**

| Code         | Description                                    |
| ------------ | ---------------------------------------------- |
| SSRVVRF00800 | Certificate VC data not found.                 |
| SSRVVRF00904 | Failed to process 'get-certificate-vc' API request. |

<div style="page-break-after: always; margin-top: 30px;"></div>

#### 6.2.3. Example

**■ Request**

```shell
curl -v -X GET "http://${Host}:${Port}/verifier/api/v1/certificate-vc"
```

**■ Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json;charset=utf-8

{
  "@context": [
    "https://www.w3.org/ns/credentials/v2"
  ],
  "credentialSchema": {
    "id": "http://127.0.0.1:8090/tas/api/v1/vc-schema?name=certificate",
    "type": "OsdSchemaCredential"
  },
  "credentialSubject": {
    "claims": [
      {
        "caption": "subject",
        "code": "org.opendid.v1.subject",
        "format": "plain",
        "hideValue": false,
        "type": "text",
        "value": "o=Verifier"
      },
      {
        "caption": "role",
        "code": "org.opendid.v1.role",
        "format": "plain",
        "hideValue": false,
        "type": "text",
        "value": "Verifier"
      }
    ],
    "id": "did:omn:verifier"
  },
  "encoding": "UTF-8",
  "evidence": [
    {
      "attribute": {
        "licenseNumber": "1234567890"
      },
      "documentPresence": "Physical",
      "evidenceDocument": "BusinessLicense",
      "subjectPresence": "Physical",
      "type": "DocumentVerification",
      "verifier": "did:omn:tas"
    }
  ],
  "formatVersion": "1.0",
  "id": "0815ebc5-7264-45cc-8c6c-4db17640a4b7",
  "issuanceDate": "2024-01-01T09:50:19Z",
  "issuer": {
    "id": "did:omn:tas",
    "name": "raonsecure"
  },
  "language": "ko",
  "proof": {
    "created": "2024-01-01T09:50:19Z",
    "proofPurpose": "assertionMethod",
    "proofValue": "mH8dkYyG51tSSLqrQxmdXTh...",
    "proofValueList": [
      "mIIvBGv2JJk6XSmXQnJIMU...",
      "mH58hKzV3+OI98K2P4n6Hq..."
    ],
    "type": "Secp256r1Signature2018",
    "verificationMethod": "did:omn:tas?versionId=1#assert"
  },
  "type": [
    "VerifiableCredential",
    "CertificateVC"
  ],
  "validFrom": "2024-01-01T09:50:19Z",
  "validUntil": "2025-01-01T09:50:19Z"
}
```

