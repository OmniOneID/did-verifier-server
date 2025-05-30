# Open DID Verifier Database Table Definition

- Date: 2025-05-30
- Version: v2.0.0

## 목차
- [1. 개요](#1-개요)
  - [1.1. ERD](#11-erd)
- [2. 테이블 정의](#2-테이블-정의)
  - [2.1. Transaction](#21-transaction)
  - [2.2. Sub Transaction](#22-sub-transaction)
  - [2.3. VP Offer](#23-vp-offer)
  - [2.4. VP Submit](#24-vp-submit)
  - [2.5. VP Profile](#25-vp-profile)
  - [2.6. E2E](#26-e2e)
  - [2.7. Certificate VC](#27-certificate-vc)
  - [2.8. Policy](#28-policy)
  - [2.9. Payload](#29-payload)
  - [2.10. Filter](#210-filter)
  - [2.11. Policy Profile](#211-policy-profile)
  - [2.12. Process](#212-process)
  - [2.13. Verifier](#213-verifier)
  - [2.14. Admin](#214-admin)
  - [2.15. ZKP Proof Request](#215-zkp-proof-request)
  - [2.16. ZKP Policy Profile](#216-zkp-policy-profile)

## 1. 개요

본 문서는 Verifier 서버에서 사용되는 데이터베이스 테이블의 구조를 정의합니다. 각 테이블의 필드 속성, 관계, 데이터 흐름을 설명하여 시스템 개발 및 유지보수를 위한 필수 참고 자료로 활용됩니다.

### 1.1 ERD

[ERD](https://www.erdcloud.com/d/refvZerYJ5mc5FKaa) 사이트에 접속하여 다이어그램을 확인하세요. 이 다이어그램은 Verifier 서버 데이터베이스의 테이블 간 관계를 시각적으로 나타내며, 주요 속성, 기본 키, 외래 키 관계를 포함합니다.

## 2. 테이블 정의

### 2.1. Transaction

트랜잭션 정보를 저장하는 테이블입니다.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | 아이디                                |
|      | tx_id              | VARCHAR    | 40     | NO       | N/A      | 트랜잭션 아이디                    |
|      | type               | VARCHAR    | 50     | NO       | N/A      | 트랜잭션 타입                  |
|      | status             | VARCHAR    | 50     | NO       | N/A      | 트랜잭션 상태                |
|      | expired_at         | TIMESTAMP  |        | NO       | N/A      | 만료 일시                   |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | 생성 일시                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | 수정 일시                      |

### 2.2. Sub Transaction

서브 트랜잭션 정보를 저장하는 테이블입니다.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | 아이디                                |
|      | step               | TINYINT    |        | NO       | N/A      | 단계                              |
|      | type               | VARCHAR    | 50     | NO       | N/A      | 트랜잭션 타입                  |
|      | status             | VARCHAR    | 50     | NO       | N/A      | 상태                            |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | 생성 일시                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | 수정 일시                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | 트랜잭션 키                   |

### 2.3. VP Offer

VP(Verifiable Presentation) 오퍼 정보를 저장하는 테이블입니다.

| Key  | Column Name        | Data Type  | Length | Nullable | Default      | Description                       |
|------|--------------------|------------|--------|----------|--------------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A          | 아이디                                |
|      | offer_id           | VARCHAR    | 40     | NO       | N/A          | 오퍼 아이디                          |
|      | service            | VARCHAR    | 40     | NO       | N/A          | 서비스 아이디                        |
|      | device             | VARCHAR    | 40     | NO       | N/A          | 디바이스                            |
|      | payload            | LONGTEXT   |        | NO       | N/A          | 페이로드                           |
|      | passcode           | VARCHAR    | 64     | YES      | N/A          | 패스코드                          |
|      | vp_policy_id       | VARCHAR    | 40     | NO       | N/A          | VP 정책 아이디                      |
|      | offer_type         | VARCHAR    | 40     | NO       | VerifyOffer  | 오퍼 타입                        |
|      | valid_until        | TIMESTAMP  |        | YES      | N/A          | 오퍼 유효 기한                 |
|      | created_at         | TIMESTAMP  |        | NO       | now()        | 생성 일시                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A          | 수정 일시                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A          | 트랜잭션 키                   |

### 2.4. VP Submit

VP(Verifiable Presentation) 제출 정보를 저장하는 테이블입니다.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | 아이디                                |
|      | vp                 | LONGTEXT   |        | NO       | N/A      | 검증 가능한 프레젠테이션            |
|      | vp_did             | VARCHAR    | 40     | YES      | N/A      | 검증 가능한 프레젠테이션 DID       |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | 생성 일시                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | 수정 일시                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | 트랜잭션 키                   |

### 2.5. VP Profile

VP(Verifiable Presentation) 프로파일 정보를 저장하는 테이블입니다.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | 아이디                                |
|      | profile_id         | VARCHAR    | 40     | NO       | N/A      | VP 프로파일 아이디                     |
|      | vp_profile         | LONGTEXT   |        | NO       | N/A      | VP 프로파일                        |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | 생성 일시                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | 수정 일시                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | 트랜잭션 키                   |

### 2.6. E2E

E2E (End-to-End Encryption) 정보를 저장하는 테이블입니다.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | 아이디                                |
|      | session_key        | VARCHAR    | 100    | NO       | N/A      | 세션 키                       |
|      | nonce              | VARCHAR    | 100    | NO       | N/A      | 논스                             |
|      | curve              | VARCHAR    | 20     | NO       | N/A      | 곡선                             |
|      | cipher             | VARCHAR    | 20     | NO       | N/A      | 암호화 타입                       |
|      | padding            | VARCHAR    | 20     | NO       | N/A      | 패딩                           |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | 생성 일시                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | 수정 일시                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | 트랜잭션 키                   |

### 2.7. Certificate VC

Certificate VC(Verifiable Credential) 정보를 저장하는 테이블입니다.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | 아이디                                |
|      | vc                 | TEXT       |        | NO       | N/A      | 인증서 VC 내용 (json)    |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | 생성 일시                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | 수정 일시                      |

### 2.8. Policy

VP 및 ZKP 검증에 사용되는 정책 정보를 저장하는 테이블입니다.

| Key | Column Name        | Data Type | Length | Nullable | Default | Description                                |
|-----|--------------------|-----------|---------|-----------|---------|--------------------------------------------|
| PK  | id                 | VARCHAR   | 255    | NO        | N/A     | 아이디                                         |
|     | policy_id          | VARCHAR   | 40     | NO        | N/A     | 정책 식별자                          |
|     | payload_id         | VARCHAR   | 40     | NO        | N/A     | 페이로드 식별자                         |
|     | policy_profile_id  | VARCHAR   | 40     | NO        | N/A     | 정책 프로파일 식별자                  |
|     | policy_title       | VARCHAR   | 255    | NO        | N/A     | 정책 제목                               |
|     | policy_type        | VARCHAR   | 40     | NO        | VP      | 정책 타입 (ZKP, VP)                      |
|     | created_at         | TIMESTAMP |        | NO        | now()   | 생성 일시                               |
|     | updated_at         | TIMESTAMP |        | YES       | N/A     | 수정 일시                               |

### 2.9. Payload

VP 검증 요청을 위한 페이로드 구성을 저장하는 테이블입니다.

| Key | Column Name   | Data Type | Length | Nullable | Default    | Description                                |
|-----|---------------|-----------|---------|-----------|-----------|--------------------------------------------|
| PK  | id            | BIGINT    |        | NO        | N/A       | 아이디                                         |
|     | payload_id    | VARCHAR   | 40     | NO        | N/A       | 페이로드 식별자                         |
|     | device        | VARCHAR   | 40     | NO        | N/A       | 디바이스 아이디                                  |
|     | service       | VARCHAR   | 40     | NO        | N/A       | 서비스 아이디                                 |
|     | endpoint      | VARCHAR   | 100    | YES       | N/A       | 엔드포인트 URL                               |
|     | locked        | BOOLEAN   |        | YES       | false     | 페이로드 잠금 여부              |
|     | mode          | VARCHAR   | 40     | NO        | N/A       | 요청 모드                               |
|     | valid_second  | TINYINT   |        | NO        | N/A       | 페이로드 유효 시간 (초)            |
|     | offer_type    | VARCHAR   | 40     | NO        | Offer Type| 오퍼 타입                                 |
|     | created_at    | TIMESTAMP |        | NO        | now()     | 생성 일시                               |
|     | updated_at    | TIMESTAMP |        | YES       | N/A       | 수정 일시                               |

### 2.10. Filter

검증 가능한 자격 증명의 클레임을 검증하는 데 사용되는 VP 필터링 규칙을 저장하는 테이블입니다.

| Key | Column Name     | Data Type | Length | Nullable | Default | Description                              |
|-----|-----------------|-----------|---------|----------|---------|------------------------------------------|
| PK  | filter_id       | BIGINT    |        | NO       | N/A     | 필터 식별자                        |
|     | name            | VARCHAR   | 40     | NO       | N/A     | 필터 이름                              |
|     | id              | VARCHAR   | 100    | NO       | N/A     | 필터 내부 식별자               |
|     | type            | VARCHAR   | 40     | NO       | N/A     | VC 스키마 형식 타입                    |
|     | display_claims  | VARCHAR   | 100    | YES      | N/A     | 표시할 클레임                   |
|     | required_claims | VARCHAR   | 100    | YES      | N/A     | 검증에 필요한 클레임         |
|     | present_all     | BOOLEAN   |        | YES      | N/A     | 모든 클레임 존재 필수 여부         |
|     | value           | VARCHAR   | 500    | YES      | N/A     | 클레임의 예상 값                  |
|     | allowed_issuers | VARCHAR   | 100    | YES      | N/A     | 발급자 DID 목록 (쉼표로 구분)      |
|     | created_at      | TIMESTAMP |        | NO       | now()   | 생성 일시                             |
|     | updated_at      | TIMESTAMP |        | YES      | N/A     | 수정 일시                             |

### 2.11. Policy Profile

특정 필터와 프로세스에 대해 VP를 검증하는 규칙을 정의하는 정책 프로파일을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description         |
|-----|-------------|-----------|---------|----------|---------|---------------------|
| PK  | id          | BIGINT    |        | NO       | N/A     | 아이디                  |
| PK  | process_id  | BIGINT    |        | NO       | N/A     | 프로세스 아이디          |
| PK  | filter_id   | BIGINT    |        | NO       | N/A     | 필터 아이디           |
|     | profile_id  | VARCHAR   | 40     | NO       | N/A     | 프로파일 식별자  |
|     | type        | VARCHAR   | 40     | NO       | N/A     | 프로파일 타입        |
|     | title       | VARCHAR   | 40     | NO       | N/A     | 프로파일 제목       |
|     | description | VARCHAR   | 200    | YES      | N/A     | 프로파일 설명 |
|     | encoding    | VARCHAR   | 40     | NO       | N/A     | 인코딩 형식     |
|     | language    | VARCHAR   | 40     | NO       | N/A     | 프로파일 언어 |
|     | created_at  | TIMESTAMP |        | NO       | now()   | 생성 일시        |
|     | updated_at  | TIMESTAMP |        | YES      | N/A     | 수정 일시        |

### 2.12. Process

암호화 및 인증 구성을 포함하여 VP 검증에 사용되는 암호화 프로세스 설정을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description              |
|-----|-------------|-----------|---------|----------|---------|--------------------------|
| PK  | id          | BIGINT    |        | NO       | N/A     | 아이디                       |
|     | endpoints   | VARCHAR   | 100    | YES      | N/A     | API 엔드포인트 URL         |
|     | curve       | VARCHAR   | 40     | NO       | N/A     | 타원 곡선 알고리즘 |
|     | public_key  | VARCHAR   | 40     | NO       | N/A     | 공개 키               |
|     | cipher      | VARCHAR   | 40     | NO       | N/A     | 암호화 알고리즘         |
|     | padding     | VARCHAR   | 40     | NO       | N/A     | 패딩 타입             |
|     | auth_type   | VARCHAR   | 40     | NO       | N/A     | 인증 타입                |
|     | created_at  | TIMESTAMP |        | NO       | now()   | 생성 일시             |
|     | updated_at  | TIMESTAMP |        | YES      | N/A     | 수정 일시             |

### 2.13. Verifier

검증 가능한 프레젠테이션을 받고 검증하는 검증자 정보를 저장하는 테이블입니다.

| Key | Column Name     | Data Type | Length | Nullable | Default | Description              |
|-----|-----------------|-----------|---------|----------|---------|--------------------------|
| PK  | id              | BIGINT    |        | NO       | N/A     | 아이디                       |
|     | did             | VARCHAR   | 200    | NO       | N/A     | DID                      |
|     | name            | VARCHAR   | 200    | NO       | N/A     | 이름                     |
|     | status          | VARCHAR   | 50     | NO       | N/A     | 상태                   |
|     | server_url      | VARCHAR   | 2000   | YES      | N/A     | 검증자 서버 URL      |
|     | certificate_url | VARCHAR   | 2000   | YES      | N/A     | 인증서 엔드포인트 URL |
|     | created_at      | TIMESTAMP |        | NO       | now()   | 생성 일시             |
|     | updated_at      | TIMESTAMP |        | YES      | N/A     | 수정 일시             |

### 2.14. Admin

검증자 시스템 관리를 위한 관리자 계정 정보를 저장하는 테이블입니다.

| Key  | Column Name            | Data Type  | Length | Nullable | Default  | Description                                |
|------|------------------------|------------|--------|----------|----------|--------------------------------------------|
| PK   | id                     | BIGINT     |        | NO       | N/A      | 아이디                                         |
|      | login_id               | VARCHAR    | 50     | NO       | N/A      | 관리자 로그인 아이디                     |
|      | login_password         | VARCHAR    | 64     | NO       | N/A      | 해시된 로그인 비밀번호                      |
|      | name                   | VARCHAR    | 200    | NO       | N/A      | 관리자 이름                         |
|      | email                  | VARCHAR    | 100    | YES      | N/A      | 이메일 주소                              |
|      | email_verified         | BOOLEAN    |        | YES      | false    | 이메일 인증 여부                  |
|      | require_password_reset | BOOLEAN    |        | NO       | true     | 다음 로그인 시 비밀번호 재설정 강제        |
|      | role                   | VARCHAR    | 50     | YES      | N/A      | 관리자 역할                                 |
|      | created_by             | VARCHAR    | 50     | NO       | N/A      | 생성자의 로그인 아이디                         |
|      | created_at             | TIMESTAMP  |        | NO       | now()    | 생성 일시                               |
|      | updated_at             | TIMESTAMP  |        | YES      | N/A      | 수정 일시                               |

### 2.15. ZKP Proof Request

ZKP 기반 검증을 위한 ZKP (Zero-Knowledge Proof) 증명 요청 정보를 저장하는 테이블입니다.

| Key | Column Name           | Data Type | Length | Nullable | Default | Description                                    |
|-----|-----------------------|-----------|---------|----------|---------|------------------------------------------------|
| PK  | id                    | BIGINT    |        | NO       | N/A     | 아이디                                             |
|     | name                  | VARCHAR   | 40     | NO       | N/A     | 증명 요청 이름                             |
|     | version               | VARCHAR   | 10     | NO       | N/A     | 증명 요청 버전                          |
|     | requested_attributes  | TEXT      |        | YES      | N/A     | JSON 형식의 요청된 속성            |
|     | requested_predicates  | TEXT      |        | YES      | N/A     | JSON 형식의 요청된 술어            |
|     | curve                 | VARCHAR   | 40     | NO       | N/A     | 타원 곡선 알고리즘                       |
|     | cipher                | VARCHAR   | 40     | NO       | N/A     | 암호화 알고리즘                               |
|     | padding               | VARCHAR   | 40     | NO       | N/A     | 패딩 타입                                   |
|     | created_at            | TIMESTAMP |        | NO       | now()   | 생성 일시                                   |
|     | updated_at            | TIMESTAMP |        | YES      | N/A     | 수정 일시                                   |

### 2.16. ZKP Policy Profile

ZKP 기반 검증 규칙을 정의하는 ZKP 정책 프로파일을 저장하는 테이블입니다.

| Key | Column Name           | Data Type | Length | Nullable | Default | Description                    |
|-----|-----------------------|-----------|---------|----------|---------|--------------------------------|
| PK  | id                    | BIGINT    |        | NO       | N/A     | 아이디                             |
|     | profile_id            | VARCHAR   | 40     | NO       | N/A     | ZKP 프로파일 식별자         |
|     | type                  | VARCHAR   | 40     | NO       | N/A     | 프로파일 타입                   |
|     | title                 | VARCHAR   | 40     | NO       | N/A     | 프로파일 제목                  |
|     | description           | VARCHAR   | 200    | YES      | N/A     | 프로파일 설명            |
|     | encoding              | VARCHAR   | 40     | NO       | N/A     | 인코딩 형식                |
|     | language              | VARCHAR   | 40     | NO       | N/A     | 프로파일 언어            |
|     | created_at            | TIMESTAMP |        | NO       | now()   | 생성 일시                   |
|     | updated_at            | TIMESTAMP |        | YES      | N/A     | 수정 일시                   |
| FK  | zkp_proof_request_id  | BIGINT    |        | NO       | N/A     | zkp_proof_request 테이블 참조 |