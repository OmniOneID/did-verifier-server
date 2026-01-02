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

- Date: 2025-05-28
- Version: v2.0.0

| Version | Date       | Changes                 |
|---------|------------|-------------------------|
| v1.0.0  | 2024-09-03 | 최초 버전         |
| v2.0.0  | 2025-05-28 | ZKP & ADMIN 에러 추가   |


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

# 모델
## Error Response

### 설명
```
Verifier Backend용 에러 구조체. 코드와 메시지 쌍을 가지고 있습니다.
코드는 SSRVVRF로 시작합니다.
```

### 선언
```java
public class ErrorResponse {
    private final String code;
    private final String description;
}
```

### 속성

| Name        | Type   | Description                        | **M/O** | **Note** |
|-------------|--------|------------------------------------|---------| -------- |
| code        | String | 에러 코드. SSRVVRF로 시작 | M       |          | 
| message     | String | 에러 설명                  | M       |          | 

# 에러 코드

## 1-1. General Errors (100-199)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00100 | Unable to process the request.                 | 400         | 요청 형식과 내용을 확인하세요.             |
| SSRVVRF00101 | Failed to parse JSON.                          | 500         | JSON 형식과 구조를 확인하세요.               |
| SSRVVRF00102 | Failed to retrieve DID document on the blockchain. | 500    | 블록체인 연결과 DID 유효성을 확인하세요.  |
| SSRVVRF00103 | Failed to verify signature.                    | 500         | 서명 생성 프로세스를 확인하세요.           |
| SSRVVRF00104 | Failed to generate hash.                       | 500         | 해싱 알고리즘과 입력 데이터를 확인하세요.        |
| SSRVVRF00105 | Failed to encode data.                         | 500         | 인코딩 프로세스와 입력 데이터를 확인하세요.        |
| SSRVVRF00106 | Failed to decode data : incorrect encoding     | 400         | 인코딩된 데이터와 디코딩 방법을 확인하세요.        |
| SSRVVRF00107 | Failed to generate signature.                  | 500         | 서명 생성 프로세스와 키를 확인하세요.  |
| SSRVVRF00108 | Failed to ping the URL.                        | 400         | URL 접근성과 네트워크 연결을 확인하세요. |

<br>

## 1-2. VP Related Errors (200-299)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00200 | VP_OFFER is not found.                         | 400         | VP 오퍼 존재 여부와 유효성을 확인하세요.         |
| SSRVVRF00201 | VP_POLICY is not found.                        | 400         | VP 정책 구성을 확인하세요.                |
| SSRVVRF00202 | VP verification failed.                        | 500         | VP 형식과 검증 프로세스를 확인하세요.      |
| SSRVVRF00203 | VP_PROFILE is not found.                       | 500         | VP 프로파일 존재 여부와 구성을 확인하세요. |
| SSRVVRF00204 | Failed to parse VP profile.                    | 500         | VP 프로파일 형식과 구조를 확인하세요.         |
| SSRVVRF00205 | Failed to parse verify profile.                | 500         | 검증 프로파일 형식과 구조를 확인하세요.     |
| SSRVVRF00206 | Failed to read VP policy.                      | 500         | VP 정책 데이터와 접근 권한을 확인하세요.   |
| SSRVVRF00207 | VP_PAYLOAD is not found.                       | 400         | VP 페이로드 존재 여부와 형식을 확인하세요.        |
| SSRVVRF00208 | VP_POLICY_PROFILE is not found.                | 400         | VP 정책 프로파일 구성을 확인하세요.         |
| SSRVVRF00209 | VP_PROCESS is not found.                       | 400         | VP 프로세스 구성을 확인하세요.               |
| SSRVVRF00210 | VP_FILTER is not found.                        | 400         | VP 필터 구성을 확인하세요.                 |
| SSRVVRF00211 | Failed to update VP policy.                    | 500         | VP 정책 업데이트 프로세스와 권한을 확인하세요. |
| SSRVVRF00212 | PAYLOAD is in use by one or more policies      | 400         | 삭제 전에 페이로드 종속성을 제거하세요.   |
| SSRVVRF00213 | POLICY_PROFILE is in use by one or more policies | 400       | 삭제 전에 정책 프로파일 종속성을 제거하세요. |
| SSRVVRF00214 | VP_FILTER is in use by one or more profile     | 400         | 삭제 전에 필터 종속성을 제거하세요.    |
| SSRVVRF00215 | VP_PROCESS is in use by one or more profile.   | 400         | 삭제 전에 프로세스 종속성을 제거하세요.   |
| SSRVVRF00216 | VC_SCHEMA not found                            | 500         | VC 스키마 존재 여부와 구성을 확인하세요.   |

<br>

## 1-3. Transaction Errors (300-399)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00300 | Transaction not found.                         | 400         | 트랜잭션 ID와 존재 여부를 확인하세요.            |
| SSRVVRF00301 | Transaction status is not pending.             | 400         | 트랜잭션 상태와 워크플로우를 확인하세요.        |
| SSRVVRF00302 | Transaction has expired.                       | 400         | 새 트랜잭션을 생성하거나 만료 시간을 연장하세요.      |
| SSRVVRF00303 | Subtransaction not found.                      | 400         | 서브트랜잭션 ID와 존재 여부를 확인하세요.         |
| SSRVVRF00304 | Subtransaction status is invalid.              | 400         | 서브트랜잭션 상태와 워크플로우를 확인하세요.     |

<br>

## 1-4. Authentication and Cryptography Errors (400-499)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00400 | Invalid AuthType: Type mismatch.               | 400         | 인증 타입과 구성을 확인하세요.   |
| SSRVVRF00401 | Crypto error occurred.                         | 500         | 암호화 작업과 매개변수를 확인하세요. |
| SSRVVRF00402 | Invalid nonce.                                 | 400         | 논스 생성과 유효성을 확인하세요.          |
| SSRVVRF00403 | Invalid proof purpose.                         | 400         | 증명 목적 형식과 유효성을 확인하세요.       |
| SSRVVRF00404 | Failed to merge nonce.                         | 500         | 논스 병합 알고리즘과 프로세스를 확인하세요.     |
| SSRVVRF00405 | Failed to generate shared secret.              | 500         | 공유 비밀 생성 프로세스를 확인하세요.       |
| SSRVVRF00406 | Failed to merge shared secret and nonce.       | 500         | 비밀과 논스 병합 알고리즘을 확인하세요.  |
| SSRVVRF00407 | Failed to generate nonce.                      | 500         | 논스 생성 알고리즘을 확인하세요.             |
| SSRVVRF00408 | Failed to generate key pair.                   | 500         | 키 생성 알고리즘과 매개변수를 확인하세요. |
| SSRVVRF00409 | Failed to decrypt data.                        | 500         | 복호화 프로세스와 키를 확인하세요.            |
| SSRVVRF00410 | Invalid ECC curve type.                        | 500         | ECC 곡선 구성과 지원을 확인하세요.     |
| SSRVVRF00411 | Invalid symmetric cipher type.                 | 500         | 대칭 암호 타입과 구성을 확인하세요. |
| SSRVVRF00412 | Invalid symmetric padding type.                | 500         | 대칭 패딩 타입과 구성을 확인하세요. |

<br>

## 1-5. Wallet and Key Management Errors (500-599)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00500 | Failed to connect to wallet.                   | 500         | 지갑 연결 설정과 네트워크를 확인하세요.  |
| SSRVVRF00501 | Failed to generate wallet signature.           | 500         | 지갑 서명 생성 프로세스를 확인하세요.    |
| SSRVVRF00502 | Failed to compress public key.                 | 500         | 공개 키 압축 알고리즘을 확인하세요.        |
| SSRVVRF00503 | Failed to get File Wallet Manager.             | 500         | File Wallet Manager 초기화를 확인하세요.      |
| SSRVVRF00504 | Failed to create wallet: wallet already exists. | 500        | 기존 지갑을 사용하거나 다른 이름을 선택하세요.  |
| SSRVVRF00505 | Failed to create wallet: invalid wallet file path. | 500     | 지갑 파일 경로와 권한을 확인하세요.       |
| SSRVVRF00506 | Failed to generate keys: key already exists.   | 500         | 기존 키를 사용하거나 다른 식별자를 선택하세요. |
| SSRVVRF00507 | Failed to load key element.                    | 500         | 키 요소 형식과 접근성을 확인하세요.    |
| SSRVVRF00508 | Failed to create wallet.                       | 500         | 지갑 생성 프로세스와 요구사항을 확인하세요. |

<br>

## 1-6. DID Related Errors (600-699)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00600 | Failed to retrieve DID Document.               | 500         | DID 문서 접근성과 형식을 확인하세요.   |
| SSRVVRF00601 | Failed to find DID document.                   | 500         | DID 문서 존재 여부와 위치를 확인하세요.    |
| SSRVVRF00602 | Invalid DID Document.                          | 400         | DID 문서 형식과 구조를 확인하세요.       |
| SSRVVRF00603 | Verifier is already registered.                | 400         | 기존 검증자를 사용하거나 등록을 업데이트하세요.  |
| SSRVVRF00604 | Failed to register Verifier DID Document: document is already registered. | 400 | 기존 문서를 사용하거나 등록을 업데이트하세요. |
| SSRVVRF00605 | Failed to generate DID document.               | 500         | DID 문서 생성 프로세스를 확인하세요.         |
| SSRVVRF00606 | Failed to register Verifier DID Document.      | 500         | 등록 프로세스와 요구사항을 확인하세요.  |
| SSRVVRF00607 | Failed to find Verifier DID Document: o registration request has been made. | 400 | 먼저 등록 프로세스를 완료하세요. |
| SSRVVRF00608 | Failed to register Verifier DID Document: document is already requested. | 400 | 기존 요청이 완료될 때까지 기다리세요. |
| SSRVVRF00609 | Failed to process certificate VC: invalid JSON format. | 500 | 인증서 VC JSON 형식과 구조를 확인하세요. |

<br>

## 1-7. E2E Related Errors (700-799)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00700 | E2E is not found.                              | 400         | E2E 구성과 존재 여부를 확인하세요.         |
| SSRVVRF00701 | E2E is invalid.                                | 400         | E2E 형식과 유효성을 확인하세요.                |
| SSRVVRF00702 | Failed to generate key.                        | 500         | 키 생성 프로세스와 매개변수를 확인하세요.   |

<br>

## 1-8. Certificate VC Errors (800-899)

| Error Code   | Error Message                  | HTTP Status | Action Required                                |
|--------------|--------------------------------|-------------|------------------------------------------------|
| SSRVVRF00800 | Certificate VC data not found. | 500         | 인증서 VC 데이터 존재 여부와 저장소를 확인하세요. |
| SSRVVRF00801 | VC Status is not valid.        | 400         | 발급받은 VC의 상태가 유효하지 않습니다. VC 상태를 확인하세요. |

<br>

## 1-9. API Process Errors (900-999)

| Error Code   | Error Message                                           | HTTP Status | Action Required                                |
|--------------|----------------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF00900 | Failed to process the 'request-offer-qr' API request.  | 500         | request-offer-qr API 매개변수와 프로세스를 확인하세요. |
| SSRVVRF00901 | Failed to process the 'confirm-verify' API request.    | 500         | confirm-verify API 요청 형식을 확인하세요.     |
| SSRVVRF00902 | Failed to process the 'request-profile' API request.   | 500         | request-profile API 매개변수를 확인하세요.         |
| SSRVVRF00903 | Failed to process the 'request-verify' API request.    | 500         | request-verify API 형식과 데이터를 확인하세요.    |
| SSRVVRF00904 | Failed to process the 'get-certificate-vc' API request. | 500        | get-certificate-vc API 프로세스를 확인하세요.         |
| SSRVVRF00905 | Failed to process the 'issue-certificate-vc' API request. | 500      | issue-certificate-vc API 매개변수를 확인하세요.   |
| SSRVVRF00906 | Failed to process the 'request-proof-request-profile' API request. | 500 | request-proof-request-profile API 형식을 확인하세요. |

<br>

## 1-10. Verifier Errors (1000-1099)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF01000 | Failed to find verifier: verifier is not registered. | 500      | 사용 전에 검증자를 등록하세요.               |

<br>

## 1-11. Admin Errors (1100-1199)

| Error Code   | Error Message                                  | HTTP Status | Action Required                                |
|--------------|------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF01100 | Failed to find admin: admin is not registered. | 400        | 사용 전에 관리자 계정을 등록하세요.          |
| SSRVVRF01101 | Failed to register admin: admin is already registered. | 400    | 기존 관리자 계정을 사용하거나 업데이트하세요.         |
| SSRVVRF01102 | Failed to communicate with tas: unknown error occurred. | 500   | TAS 연결과 구성을 확인하세요.        |
| SSRVVRF01103 | Failed to process response: received unknown data from the tas. | 500 | TAS 응답 형식과 호환성을 확인하세요. |

<br>

## 1-12. ZKP Errors (1200-1299)

| Error Code   | Error Message                                           | HTTP Status | Action Required                                |
|--------------|----------------------------------------------------------|-------------|------------------------------------------------|
| SSRVVRF01201 | Failed to find Proof request profile : request proof profile not found | 400 | 증명 요청 프로파일 존재 여부와 구성을 확인하세요. |
| SSRVVRF01202 | Failed to find ZKP policy profile : request proof profile not found | 400 | ZKP 정책 프로파일 구성을 확인하세요.       |
| SSRVVRF01203 | Failed to parse proof request profile : request proof profile parse error | 500 | 증명 요청 프로파일 형식과 구조를 확인하세요. |
| SSRVVRF01204 | Failed to find ZKP proof request : request proof profile not found | 400 | ZKP 증명 요청 데이터와 형식을 확인하세요.      |
| SSRVVRF01205 | Failed to verify proof : proof verify failed           | 500         | ZKP 증명 유효성과 검증 프로세스를 확인하세요. |
| SSRVVRF01206 | Failed to retrieve ZKP credential on the blockchain.   | 500         | 블록체인 연결과 ZKP 자격 증명 데이터를 확인하세요. |
| SSRVVRF01207 | Failed to retrieve ZKP credential definition on the blockchain. | 500 | 블록체인 연결과 자격 증명 정의를 확인하세요. |
| SSRVVRF01208 | Failed to find credential schema : credential schema not found | 400 | 자격 증명 스키마 존재 여부와 형식을 확인하세요.   |
| SSRVVRF01209 | Failed to find proof request : proof request not found | 400         | 증명 요청 데이터와 구성을 확인하세요.    |
    