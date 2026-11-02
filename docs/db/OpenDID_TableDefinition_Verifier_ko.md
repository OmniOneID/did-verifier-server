# Open DID Verifier Database Table Definition

- Date: 2026-11-02
- Version: v3.0.0

| Version | Date       | Changes |
| ------- | ---------- | ------- |
| v1.0.0  | 2024-10-28 | 최초 버전 |
| v2.0.0  | 2025-05-30 | 관리자 콘솔·ZKP 테이블 추가 |
| v3.0.0  | 2026-11-02 | OID4VP·서버 설정·비밀번호 정책·API 로그 테이블 추가, 컬럼 정의를 현재 스키마에 맞춤 |

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
  - [2.17. DID Document](#217-did-document)
  - [2.18. Server Config](#218-server-config)
  - [2.19. Admin Password Policy](#219-admin-password-policy)
  - [2.20. API Log](#220-api-log)
  - [2.21. OID4VP Config](#221-oid4vp-config)
  - [2.22. DCQL Scope Mapping](#222-dcql-scope-mapping)
  - [2.23. OID4VP Session](#223-oid4vp-session)
  - [2.24. OID4VP Session Mapping](#224-oid4vp-session-mapping)

## 1. 개요

본 문서는 Verifier 서버에서 사용되는 데이터베이스 테이블의 구조를 정의합니다. 각 테이블의 필드 속성, 관계, 데이터 흐름을 설명하여 시스템 개발 및 유지보수를 위한 필수 참고 자료로 활용됩니다.

### 1.1 ERD

[ERD](https://www.erdcloud.com/d/refvZerYJ5mc5FKaa) 사이트에 접속하여 다이어그램을 확인하세요. 이 다이어그램은 Verifier 서버 데이터베이스의 테이블 간 관계를 시각적으로 나타내며, 주요 속성, 기본 키, 외래 키 관계를 포함합니다.

## 2. 테이블 정의

### 2.1. Transaction

트랜잭션 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | tx_id | VARCHAR | 40 | NO | N/A | 트랜잭션 아이디 |
|  | type | VARCHAR | 50 | NO | N/A | 트랜잭션 타입 |
|  | status | VARCHAR | 50 | NO | N/A | 트랜잭션 상태 |
|  | expired_at | TIMESTAMP |  | YES | N/A | 만료 일시 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.2. Sub Transaction

서브 트랜잭션 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | step | SMALLINT |  | NO | N/A | 단계 |
|  | type | VARCHAR | 50 | NO | N/A | 트랜잭션 타입 |
|  | status | VARCHAR | 50 | NO | N/A | 상태 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
| FK | transaction_id | BIGINT |  | NO | N/A | 트랜잭션 키 |

### 2.3. VP Offer

VP(Verifiable Presentation) 오퍼 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | offer_id | VARCHAR | 40 | NO | N/A | 오퍼 아이디 |
|  | service | VARCHAR | 40 | NO | N/A | 서비스 아이디 |
|  | device | VARCHAR | 40 | NO | N/A | 디바이스 |
|  | payload | TEXT |  | NO | N/A | 페이로드 |
|  | valid_until | TIMESTAMP |  | YES | N/A | 오퍼 유효 기한 |
|  | passcode | VARCHAR | 64 | YES | N/A | 패스코드 |
|  | vp_policy_id | VARCHAR | 40 | YES | N/A | VP 정책 아이디 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
| FK | transaction_id | BIGINT |  | NO | N/A | 트랜잭션 키 |
|  | offer_type | VARCHAR | 40 | YES | N/A | 오퍼 타입 |

### 2.4. VP Submit

VP(Verifiable Presentation) 제출 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | vp | TEXT |  | YES | N/A | 검증 가능한 프레젠테이션 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
| FK | transaction_id | BIGINT |  | NO | N/A | 트랜잭션 키 |
|  | holder_did | VARCHAR | 100 | YES | N/A | 홀더 DID |
|  | error_code | VARCHAR | 100 | YES | N/A | 검증 실패 시 에러 코드 |
|  | format | VARCHAR | 50 | YES | N/A | 제출된 크리덴셜 형식(예: opendid_vc, dc+sd-jwt, mso_mdoc) |

### 2.5. VP Profile

VP(Verifiable Presentation) 프로파일 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | profile_id | VARCHAR | 40 | YES | N/A | VP 프로파일 아이디 |
|  | vp_profile | TEXT |  | NO | N/A | VP 프로파일 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
| FK | transaction_id | BIGINT |  | NO | N/A | 트랜잭션 키 |

### 2.6. E2E

E2E (End-to-End Encryption) 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | session_key | VARCHAR | 300 | NO | N/A | 세션 키 |
|  | nonce | VARCHAR | 100 | NO | N/A | 논스 |
|  | curve | VARCHAR | 20 | NO | N/A | 곡선 |
|  | cipher | VARCHAR | 20 | NO | N/A | 암호화 타입 |
|  | padding | VARCHAR | 20 | NO | N/A | 패딩 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
| FK | transaction_id | BIGINT |  | NO | N/A | 트랜잭션 키 |

### 2.7. Certificate VC

Certificate VC(Verifiable Credential) 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | vc | TEXT |  | NO | N/A | 인증서 VC 내용 (json) |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.8. Policy

VP 및 ZKP 검증에 사용되는 정책 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | policy_id | VARCHAR | 40 | NO | N/A | 정책 식별자 |
|  | payload_id | VARCHAR | 40 | YES | N/A | 페이로드 식별자 |
|  | policy_profile_id | VARCHAR | 40 | YES | N/A | 정책 프로파일 식별자 |
|  | policy_title | VARCHAR | 255 | NO | N/A | 정책 제목 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
|  | policy_type | VARCHAR | 40 | NO | N/A | 정책 타입 (ZKP, VP) |
|  | protocol_type | VARCHAR | 20 | NO | N/A | 검증 프로토콜(DID_VP, OID4VP) |
|  | scope | VARCHAR | 100 | YES | N/A | DCQL query와 연결되는 OID4VP scope(dcql_scope_mapping.scope) |

### 2.9. Payload

VP 검증 요청을 위한 페이로드 구성을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | payload_id | VARCHAR | 40 | YES | N/A | 페이로드 식별자 |
|  | device | VARCHAR | 40 | NO | N/A | 디바이스 아이디 |
|  | service | VARCHAR | 40 | NO | N/A | 서비스 아이디 |
|  | endpoints | VARCHAR | 200 | NO | N/A | 엔드포인트 URL |
|  | locked | BOOLEAN |  | NO | N/A | 페이로드 잠금 여부 |
|  | mode | VARCHAR | 40 | NO | N/A | 요청 모드 |
|  | valid_second | SMALLINT |  | NO | N/A | 페이로드 유효 시간 (초) |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
|  | offer_type | VARCHAR | 40 | YES | N/A | 오퍼 타입 |

### 2.10. Filter

검증 가능한 자격 증명의 클레임을 검증하는 데 사용되는 VP 필터링 규칙을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | filter_id | BIGINT |  | NO | N/A | 필터 식별자 |
|  | title | VARCHAR | 100 | NO | N/A | 필터 이름 |
|  | id | VARCHAR | 255 | NO | N/A | 필터 내부 식별자 |
|  | type | VARCHAR | 40 | NO | N/A | VC 스키마 형식 타입 |
|  | required_claims | VARCHAR | 255 | YES | N/A | 검증에 필요한 클레임 |
|  | allowed_issuers | VARCHAR | 255 | YES | N/A | 발급자 DID 목록 (쉼표로 구분) |
|  | display_claims | VARCHAR | 255 | YES | N/A | 표시할 클레임 |
|  | value | TEXT |  | YES | N/A | 클레임의 예상 값 |
|  | present_all | BOOLEAN |  | NO | N/A | 모든 클레임 존재 필수 여부 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.11. Policy Profile

특정 필터와 프로세스에 대해 VP를 검증하는 규칙을 정의하는 정책 프로파일을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | policy_profile_id | VARCHAR | 40 | NO | N/A | 프로파일 식별자 |
|  | type | VARCHAR | 20 | NO | N/A | 프로파일 타입 |
|  | title | VARCHAR | 100 | NO | N/A | 프로파일 제목 |
|  | description | VARCHAR | 1000 | YES | N/A | 프로파일 설명 |
|  | encoding | VARCHAR | 10 | NO | N/A | 인코딩 형식 |
|  | language | VARCHAR | 10 | NO | N/A | 프로파일 언어 |
|  | format | VARCHAR | 40 | YES | N/A | 로고 이미지 형식 |
|  | link | VARCHAR | 100 | YES | N/A | 로고 이미지 링크 |
|  | value | VARCHAR | 1000 | YES | N/A | 로고 이미지 값 |
|  | process_id | BIGINT |  | NO | N/A | 프로세스 아이디 |
|  | filter_id | BIGINT |  | NO | N/A | 필터 아이디 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
|  | profile_type | VARCHAR | 40 | YES | N/A | 프로파일 유형 |

### 2.12. Process

암호화 및 인증 구성을 포함하여 VP 검증에 사용되는 암호화 프로세스 설정을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | endpoints | VARCHAR | 400 | NO | N/A | API 엔드포인트 URL |
|  | title | VARCHAR | 100 | NO | N/A | 프로세스 제목 |
|  | auth_type | INTEGER |  | NO | N/A | 인증 타입 |
|  | curve | VARCHAR | 255 | YES | N/A | 타원 곡선 알고리즘 |
|  | cipher | VARCHAR | 255 | YES | N/A | 암호화 알고리즘 |
|  | padding | VARCHAR | 255 | YES | N/A | 패딩 타입 |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.13. Verifier

검증 가능한 프레젠테이션을 받고 검증하는 검증자 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | did | VARCHAR | 200 | YES | N/A | DID |
|  | name | VARCHAR | 200 | NO | N/A | 이름 |
|  | status | VARCHAR | 50 | NO | N/A | 상태 |
|  | server_url | VARCHAR | 2000 | YES | N/A | 검증자 서버 URL |
|  | certificate_url | VARCHAR | 2000 | YES | N/A | 인증서 엔드포인트 URL |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
|  | published_certificate_url | VARCHAR | 2000 | YES | N/A | List Provider에 게시된 가입증명서 VC URL |

### 2.14. Admin

검증자 시스템 관리를 위한 관리자 계정 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | login_id | VARCHAR | 50 | NO | N/A | 관리자 로그인 아이디 |
|  | login_password | VARCHAR | 64 | NO | N/A | 해시된 로그인 비밀번호 |
|  | name | VARCHAR | 200 | YES | N/A | 관리자 이름 |
|  | email_verified | BOOLEAN |  | NO | false | 이메일 인증 여부 |
|  | require_password_reset | BOOLEAN |  | NO | true | 다음 로그인 시 비밀번호 재설정 강제 |
|  | role | VARCHAR | 50 | NO | N/A | 관리자 역할 |
|  | created_by | VARCHAR | 50 | NO | N/A | 생성자의 로그인 아이디 |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
|  | last_password_changed_at | TIMESTAMP |  | YES | N/A | 마지막 비밀번호 변경 일시 |
|  | password_reset_reason | VARCHAR | 20 | YES | N/A | 비밀번호 재설정이 필요한 사유 |

### 2.15. ZKP Proof Request

ZKP 기반 검증을 위한 ZKP (Zero-Knowledge Proof) 증명 요청 정보를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | name | VARCHAR | 40 | NO | N/A | 증명 요청 이름 |
|  | version | VARCHAR | 10 | NO | N/A | 증명 요청 버전 |
|  | requested_attributes | TEXT |  | YES | N/A | JSON 형식의 요청된 속성 |
|  | requested_predicates | TEXT |  | YES | N/A | JSON 형식의 요청된 술어 |
|  | curve | VARCHAR | 40 | NO | N/A | 타원 곡선 알고리즘 |
|  | cipher | VARCHAR | 40 | NO | N/A | 암호화 알고리즘 |
|  | padding | VARCHAR | 40 | NO | N/A | 패딩 타입 |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.16. ZKP Policy Profile

ZKP 기반 검증 규칙을 정의하는 ZKP 정책 프로파일을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | profile_id | VARCHAR | 40 | NO | N/A | ZKP 프로파일 식별자 |
|  | type | VARCHAR | 40 | NO | N/A | 프로파일 타입 |
|  | title | VARCHAR | 40 | NO | N/A | 프로파일 제목 |
|  | description | VARCHAR | 200 | YES | N/A | 프로파일 설명 |
|  | encoding | VARCHAR | 40 | NO | N/A | 인코딩 형식 |
|  | language | VARCHAR | 40 | NO | N/A | 프로파일 언어 |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
| FK | zkp_proof_request_id | BIGINT |  | NO | N/A | zkp_proof_request 테이블 참조 |

### 2.17. DID Document

Verifier의 DID Document를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | did_document | TEXT |  | NO | N/A | DID Document(JSON) |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.18. Server Config

LSS API Key 등 외부 서비스 연동 설정을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | config_key | VARCHAR | 100 | NO | N/A | 설정 키(예: API_KEY_LSS) |
|  | config_value | TEXT |  | NO | N/A | 설정 값 |
|  | description | VARCHAR | 200 | YES | N/A | 설명 |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.19. Admin Password Policy

관리자 계정의 비밀번호 정책을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | min_length | INTEGER |  | NO | N/A | 비밀번호 최소 길이 |
|  | require_uppercase | BOOLEAN |  | NO | N/A | 대문자 포함 필수 여부 |
|  | require_number | BOOLEAN |  | NO | N/A | 숫자 포함 필수 여부 |
|  | require_special | BOOLEAN |  | NO | N/A | 특수문자 포함 필수 여부 |
|  | password_expiry_days | INTEGER |  | NO | N/A | 비밀번호 만료 기간(일) |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.20. API Log

API 호출 로그와 감사 로그를 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | method | VARCHAR | 10 | NO | N/A | HTTP 메서드 |
|  | uri | VARCHAR | 1048 | NO | N/A | 요청 URI |
|  | status | INTEGER |  | NO | N/A | HTTP 상태 코드 |
|  | result | VARCHAR | 20 | NO | N/A | 처리 결과(성공·실패) |
|  | api_name | VARCHAR | 255 | YES | N/A | API 이름 |
|  | api_description | VARCHAR | 1000 | YES | N/A | API 설명 |
|  | requester_id | VARCHAR | 128 | YES | N/A | 요청자 아이디 |
|  | duration_ms | BIGINT |  | NO | N/A | 처리 시간(ms) |
|  | client_ip | VARCHAR | 64 | YES | N/A | 클라이언트 IP |
|  | error_code | VARCHAR | 64 | YES | N/A | 에러 코드 |
|  | message | TEXT |  | YES | N/A | 메시지 |
|  | api_type | VARCHAR | 20 | YES | N/A | API 유형(AGENT, ADMIN) |
|  | created_at | TIMESTAMP |  | NO | N/A | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
|  | target_type | VARCHAR | 50 | YES | N/A | 감사 대상 유형 |
|  | target_id | VARCHAR | 128 | YES | N/A | 감사 대상 아이디 |
|  | user_agent | TEXT |  | YES | N/A | User-Agent 헤더 |
|  | action_type | VARCHAR | 20 | NO | N/A | 작업 유형(예: CREATE, UPDATE, DELETE) |

### 2.21. OID4VP Config

OID4VP SDK가 사용하는 OID4VP Verifier 설정(JSON)을 저장하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | type | VARCHAR | 50 | NO | OID4VP | 설정 유형 |
|  | config | TEXT |  | NO | N/A | OID4VP 설정(JSON). vpTokenEncryptionKey 포함 |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |

### 2.22. DCQL Scope Mapping

OID4VP scope와 요청할 DCQL query를 연결하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | scope | VARCHAR | 100 | NO | N/A | scope(고유) |
|  | dcql_query | TEXT |  | NO | N/A | DCQL query(JSON) |
|  | description | VARCHAR | 500 | YES | N/A | 설명 |
|  | enabled | BOOLEAN |  | NO | true | 사용 여부 |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
|  | updated_at | TIMESTAMP |  | YES | N/A | 수정 일시 |
|  | client_id_scheme | VARCHAR | 50 | YES | N/A | client_id scheme 지정. 비어 있으면 DCQL format으로 결정 |

### 2.23. OID4VP Session

OID4VP 검증 세션을 저장하는 테이블입니다. 시간 컬럼은 epoch 밀리초입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | transaction_id | VARCHAR | 100 | NO | N/A | OID4VP 트랜잭션 아이디 |
|  | state | VARCHAR | 100 | NO | N/A | state(고유) |
|  | nonce | VARCHAR | 200 | YES | N/A | 논스 |
|  | dcql_query | TEXT |  | YES | N/A | 요청한 DCQL query(JSON) |
|  | response_mode | VARCHAR | 50 | YES | N/A | 응답 모드(direct_post.jwt) |
|  | request_id | VARCHAR | 100 | YES | N/A | request_uri에 쓰는 요청 아이디 |
|  | status | VARCHAR | 30 | NO | CREATED | 세션 상태(CREATED, COMPLETED, FAILED) |
|  | client_metadata | TEXT |  | YES | N/A | 지갑에 보내는 client_metadata(JSON) |
|  | request_uri_fetched_at | BIGINT |  | YES | N/A | 지갑이 request_uri를 가져간 시각 |
|  | vp_token | TEXT |  | YES | N/A | 제출된 vp_token |
|  | created_at | BIGINT |  | NO | N/A | 생성 시각 |
|  | expires_at | BIGINT |  | YES | N/A | 만료 시각 |
|  | updated_at | BIGINT |  | YES | N/A | 수정 시각 |
|  | enc_kid | VARCHAR | 100 | YES | N/A | 응답 암호화 키의 kid |
|  | enc_private_key_jwk | TEXT |  | YES | N/A | 응답 복호화 개인 키(JWK). vpTokenEncryptionKey로 암호화해 저장 |

### 2.24. OID4VP Session Mapping

Verifier 트랜잭션과 OID4VP 세션을 연결하는 테이블입니다.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | 아이디 |
|  | tx_id | VARCHAR | 40 | NO | N/A | Verifier 트랜잭션 아이디(transaction.tx_id) |
|  | oid4vp_transaction_id | VARCHAR | 100 | NO | N/A | OID4VP 트랜잭션 아이디 |
|  | oid4vp_request_id | VARCHAR | 100 | NO | N/A | OID4VP 요청 아이디 |
|  | state | VARCHAR | 100 | NO | N/A | OID4VP state |
|  | created_at | TIMESTAMP |  | NO | now() | 생성 일시 |
