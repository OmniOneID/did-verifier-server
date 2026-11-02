# Open DID Verifier Database Table Definition

- Date: 2026-11-02
- Version: v3.0.0

| Version | Date       | Changes |
| ------- | ---------- | ------- |
| v1.0.0  | 2024-10-28 | Initial version |
| v2.0.0  | 2025-05-30 | Added admin console and ZKP tables |
| v3.0.0  | 2026-11-02 | Added OID4VP, server configuration, password policy, and API log tables; aligned column definitions with the current schema |

## Contents
- [1. Overview](#1-overview)
  - [1.1. ERD](#11-erd)
- [2. Table Definition](#2-table-definition)
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

## 1. Overview

This document defines the structure of the database tables used in the Verifier server. It describes the field attributes, relationships, and data flow for each table, serving as essential reference material for system development and maintenance.

### 1.1 ERD

Access the [ERD](https://www.erdcloud.com/d/refvZerYJ5mc5FKaa) site to view the diagram, which visually represents the relationships between the tables in the Verifier server database, including key attributes, primary keys, and foreign key relationships.

## 2. Table Definition

### 2.1. Transaction

This table stores transaction information.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | tx_id | VARCHAR | 40 | NO | N/A | transaction id |
|  | type | VARCHAR | 50 | NO | N/A | transaction type |
|  | status | VARCHAR | 50 | NO | N/A | transaction status |
|  | expired_at | TIMESTAMP |  | YES | N/A | expiration date |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.2. Sub Transaction

This table stores sub-transaction information.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | step | SMALLINT |  | NO | N/A | step |
|  | type | VARCHAR | 50 | NO | N/A | transaction type |
|  | status | VARCHAR | 50 | NO | N/A | status |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
| FK | transaction_id | BIGINT |  | NO | N/A | transaction key |

### 2.3. VP Offer

This table stores VP(Verifiable Presentation) offer information.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | offer_id | VARCHAR | 40 | NO | N/A | Offer id |
|  | service | VARCHAR | 40 | NO | N/A | service id |
|  | device | VARCHAR | 40 | NO | N/A | device |
|  | payload | TEXT |  | NO | N/A | payload |
|  | valid_until | TIMESTAMP |  | YES | N/A | offer valid until |
|  | passcode | VARCHAR | 64 | YES | N/A | passcode |
|  | vp_policy_id | VARCHAR | 40 | YES | N/A | vp policy id |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
| FK | transaction_id | BIGINT |  | NO | N/A | transaction Key |
|  | offer_type | VARCHAR | 40 | YES | N/A | offer type |

### 2.4. VP Submit

This table stores VP(Verifiable Presentation) submission information.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | vp | TEXT |  | YES | N/A | verifiable presentation |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
| FK | transaction_id | BIGINT |  | NO | N/A | transaction key |
|  | holder_did | VARCHAR | 100 | YES | N/A | holder DID |
|  | error_code | VARCHAR | 100 | YES | N/A | error code when verification failed |
|  | format | VARCHAR | 50 | YES | N/A | credential format of the submission (e.g. opendid_vc, dc+sd-jwt, mso_mdoc) |

### 2.5. VP Profile

This table stores VP(Verifiable Presentation) profile information.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | profile_id | VARCHAR | 40 | YES | N/A | vp profile id |
|  | vp_profile | TEXT |  | NO | N/A | vp profile |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
| FK | transaction_id | BIGINT |  | NO | N/A | transaction key |

### 2.6. E2E

This table stores E2E (End-to-End Encryption) information.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | session_key | VARCHAR | 300 | NO | N/A | session key |
|  | nonce | VARCHAR | 100 | NO | N/A | nonce |
|  | curve | VARCHAR | 20 | NO | N/A | curve |
|  | cipher | VARCHAR | 20 | NO | N/A | cipher type |
|  | padding | VARCHAR | 20 | NO | N/A | padding |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
| FK | transaction_id | BIGINT |  | NO | N/A | transaction key |

### 2.7. Certificate VC

This table stores Certificate VC(Verifiable Credential) information.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | vc | TEXT |  | NO | N/A | certificate VC contents (json) |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.8. Policy

This table stores policy information used for verifying VP and ZKP.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | policy_id | VARCHAR | 40 | NO | N/A | policy identifier |
|  | payload_id | VARCHAR | 40 | YES | N/A | payload identifier |
|  | policy_profile_id | VARCHAR | 40 | YES | N/A | policy profile identifier |
|  | policy_title | VARCHAR | 255 | NO | N/A | policy title |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
|  | policy_type | VARCHAR | 40 | NO | N/A | policy type (ZKP, VP) |
|  | protocol_type | VARCHAR | 20 | NO | N/A | verification protocol (DID_VP, OID4VP) |
|  | scope | VARCHAR | 100 | YES | N/A | OID4VP scope mapped to a DCQL query (dcql_scope_mapping.scope) |

### 2.9. Payload

This table stores payload configuration for VP verification request.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | payload_id | VARCHAR | 40 | YES | N/A | payload identifier |
|  | device | VARCHAR | 40 | NO | N/A | device id |
|  | service | VARCHAR | 40 | NO | N/A | service id |
|  | endpoints | VARCHAR | 200 | NO | N/A | endpoint url |
|  | locked | BOOLEAN |  | NO | N/A | whether the payload is locked |
|  | mode | VARCHAR | 40 | NO | N/A | request mode |
|  | valid_second | SMALLINT |  | NO | N/A | payload valid time (in seconds) |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
|  | offer_type | VARCHAR | 40 | YES | N/A | offer type |

### 2.10. Filter

This table stores VP filtering rules used to validate claims from verifiable credentials.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | filter_id | BIGINT |  | NO | N/A | filter identifier |
|  | title | VARCHAR | 100 | NO | N/A | filter name |
|  | id | VARCHAR | 255 | NO | N/A | filter internal identifier |
|  | type | VARCHAR | 40 | NO | N/A | vc schema format type |
|  | required_claims | VARCHAR | 255 | YES | N/A | claims required for verification |
|  | allowed_issuers | VARCHAR | 255 | YES | N/A | comma-separated list of issuer dids |
|  | display_claims | VARCHAR | 255 | YES | N/A | claims to be displayed |
|  | value | TEXT |  | YES | N/A | expected value of claim |
|  | present_all | BOOLEAN |  | NO | N/A | require all claims to be present |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.11. Policy Profile

This table stores policy profiles that define rules for verifying VP against a specific filter and process.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | policy_profile_id | VARCHAR | 40 | NO | N/A | profile identifier |
|  | type | VARCHAR | 20 | NO | N/A | profile type |
|  | title | VARCHAR | 100 | NO | N/A | profile title |
|  | description | VARCHAR | 1000 | YES | N/A | profile description |
|  | encoding | VARCHAR | 10 | NO | N/A | encoding format |
|  | language | VARCHAR | 10 | NO | N/A | language of profile |
|  | format | VARCHAR | 40 | YES | N/A | logo image format |
|  | link | VARCHAR | 100 | YES | N/A | logo image link |
|  | value | VARCHAR | 1000 | YES | N/A | logo image value |
|  | process_id | BIGINT |  | NO | N/A | process id |
|  | filter_id | BIGINT |  | NO | N/A | filter id |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
|  | profile_type | VARCHAR | 40 | YES | N/A | profile type |

### 2.12. Process

This table stores cryptographic process settings used for VP verification, including encryption and authentication configuration.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | endpoints | VARCHAR | 400 | NO | N/A | api endpoint url |
|  | title | VARCHAR | 100 | NO | N/A | process title |
|  | auth_type | INTEGER |  | NO | N/A | auth type |
|  | curve | VARCHAR | 255 | YES | N/A | elliptic curve algorithm |
|  | cipher | VARCHAR | 255 | YES | N/A | cipher algorithm |
|  | padding | VARCHAR | 255 | YES | N/A | padding type |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.13. Verifier

This table stores information about verifiers that receive and validate verifiable presentations.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | did | VARCHAR | 200 | YES | N/A | did |
|  | name | VARCHAR | 200 | NO | N/A | name |
|  | status | VARCHAR | 50 | NO | N/A | status |
|  | server_url | VARCHAR | 2000 | YES | N/A | verifier server url |
|  | certificate_url | VARCHAR | 2000 | YES | N/A | certificate endpoint url |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
|  | published_certificate_url | VARCHAR | 2000 | YES | N/A | URL of the certificate VC published to the List Provider |

### 2.14. Admin

This table stores administrator account information for managing the verifier system.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | login_id | VARCHAR | 50 | NO | N/A | administrator login id |
|  | login_password | VARCHAR | 64 | NO | N/A | hashed login password |
|  | name | VARCHAR | 200 | YES | N/A | administrator name |
|  | email_verified | BOOLEAN |  | NO | false | whether email is verified |
|  | require_password_reset | BOOLEAN |  | NO | true | force password reset on next login |
|  | role | VARCHAR | 50 | NO | N/A | admin role |
|  | created_by | VARCHAR | 50 | NO | N/A | creator's login id |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
|  | last_password_changed_at | TIMESTAMP |  | YES | N/A | last password change date |
|  | password_reset_reason | VARCHAR | 20 | YES | N/A | reason a password reset is required |

### 2.15. ZKP Proof Request

This table stores ZKP (Zero-Knowledge Proof) proof request information for ZKP-based verification.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | name | VARCHAR | 40 | NO | N/A | proof request name |
|  | version | VARCHAR | 10 | NO | N/A | proof request version |
|  | requested_attributes | TEXT |  | YES | N/A | requested attributes in JSON format |
|  | requested_predicates | TEXT |  | YES | N/A | requested predicates in JSON format |
|  | curve | VARCHAR | 40 | NO | N/A | elliptic curve algorithm |
|  | cipher | VARCHAR | 40 | NO | N/A | cipher algorithm |
|  | padding | VARCHAR | 40 | NO | N/A | padding type |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.16. ZKP Policy Profile

This table stores ZKP policy profiles that define rules for ZKP-based verification.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | profile_id | VARCHAR | 40 | NO | N/A | ZKP profile identifier |
|  | type | VARCHAR | 40 | NO | N/A | profile type |
|  | title | VARCHAR | 40 | NO | N/A | profile title |
|  | description | VARCHAR | 200 | YES | N/A | profile description |
|  | encoding | VARCHAR | 40 | NO | N/A | encoding format |
|  | language | VARCHAR | 40 | NO | N/A | language of profile |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
| FK | zkp_proof_request_id | BIGINT |  | NO | N/A | reference to zkp_proof_request |

### 2.17. DID Document

This table stores the DID Document of the verifier.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | did_document | TEXT |  | NO | N/A | DID Document (JSON) |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.18. Server Config

This table stores settings for integration with external services, such as the LSS API key.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | config_key | VARCHAR | 100 | NO | N/A | configuration key (e.g. API_KEY_LSS) |
|  | config_value | TEXT |  | NO | N/A | configuration value |
|  | description | VARCHAR | 200 | YES | N/A | description |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.19. Admin Password Policy

This table stores the password policy for administrator accounts.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | min_length | INTEGER |  | NO | N/A | minimum password length |
|  | require_uppercase | BOOLEAN |  | NO | N/A | whether an uppercase letter is required |
|  | require_number | BOOLEAN |  | NO | N/A | whether a number is required |
|  | require_special | BOOLEAN |  | NO | N/A | whether a special character is required |
|  | password_expiry_days | INTEGER |  | NO | N/A | password expiry period (days) |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.20. API Log

This table stores API call logs and audit logs.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | method | VARCHAR | 10 | NO | N/A | HTTP method |
|  | uri | VARCHAR | 1048 | NO | N/A | request URI |
|  | status | INTEGER |  | NO | N/A | HTTP status code |
|  | result | VARCHAR | 20 | NO | N/A | result (success or failure) |
|  | api_name | VARCHAR | 255 | YES | N/A | API name |
|  | api_description | VARCHAR | 1000 | YES | N/A | API description |
|  | requester_id | VARCHAR | 128 | YES | N/A | requester ID |
|  | duration_ms | BIGINT |  | NO | N/A | processing time (ms) |
|  | client_ip | VARCHAR | 64 | YES | N/A | client IP |
|  | error_code | VARCHAR | 64 | YES | N/A | error code |
|  | message | TEXT |  | YES | N/A | message |
|  | api_type | VARCHAR | 20 | YES | N/A | API type (AGENT, ADMIN) |
|  | created_at | TIMESTAMP |  | NO | N/A | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
|  | target_type | VARCHAR | 50 | YES | N/A | audit target type |
|  | target_id | VARCHAR | 128 | YES | N/A | audit target ID |
|  | user_agent | TEXT |  | YES | N/A | User-Agent header |
|  | action_type | VARCHAR | 20 | NO | N/A | action type (e.g. CREATE, UPDATE, DELETE) |

### 2.21. OID4VP Config

This table stores the OID4VP verifier configuration (JSON) used by the OID4VP SDK.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | type | VARCHAR | 50 | NO | OID4VP | configuration type |
|  | config | TEXT |  | NO | N/A | OID4VP configuration (JSON). Includes vpTokenEncryptionKey |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |

### 2.22. DCQL Scope Mapping

This table maps an OID4VP scope to the DCQL query to request.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | scope | VARCHAR | 100 | NO | N/A | scope (unique) |
|  | dcql_query | TEXT |  | NO | N/A | DCQL query (JSON) |
|  | description | VARCHAR | 500 | YES | N/A | description |
|  | enabled | BOOLEAN |  | NO | true | whether the mapping is enabled |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
|  | updated_at | TIMESTAMP |  | YES | N/A | updated date |
|  | client_id_scheme | VARCHAR | 50 | YES | N/A | client_id scheme override. If null, determined from the DCQL format |

### 2.23. OID4VP Session

This table stores OID4VP verification sessions. Time columns are epoch milliseconds.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | transaction_id | VARCHAR | 100 | NO | N/A | OID4VP transaction ID |
|  | state | VARCHAR | 100 | NO | N/A | state (unique) |
|  | nonce | VARCHAR | 200 | YES | N/A | nonce |
|  | dcql_query | TEXT |  | YES | N/A | requested DCQL query (JSON) |
|  | response_mode | VARCHAR | 50 | YES | N/A | response mode (direct_post.jwt) |
|  | request_id | VARCHAR | 100 | YES | N/A | request ID used in request_uri |
|  | status | VARCHAR | 30 | NO | CREATED | session status (CREATED, COMPLETED, FAILED) |
|  | client_metadata | TEXT |  | YES | N/A | client_metadata sent to the wallet (JSON) |
|  | request_uri_fetched_at | BIGINT |  | YES | N/A | time the wallet fetched request_uri |
|  | vp_token | TEXT |  | YES | N/A | submitted vp_token |
|  | created_at | BIGINT |  | NO | N/A | created time |
|  | expires_at | BIGINT |  | YES | N/A | expiry time |
|  | updated_at | BIGINT |  | YES | N/A | updated time |
|  | enc_kid | VARCHAR | 100 | YES | N/A | kid of the response encryption key |
|  | enc_private_key_jwk | TEXT |  | YES | N/A | response decryption private key (JWK), encrypted with vpTokenEncryptionKey |

### 2.24. OID4VP Session Mapping

This table maps a verifier transaction to an OID4VP session.

| Key | Column Name | Data Type | Length | Nullable | Default | Description |
|-----|-------------|-----------|--------|----------|---------|-------------|
| PK | id | BIGINT |  | NO | N/A | id |
|  | tx_id | VARCHAR | 40 | NO | N/A | verifier transaction ID (transaction.tx_id) |
|  | oid4vp_transaction_id | VARCHAR | 100 | NO | N/A | OID4VP transaction ID |
|  | oid4vp_request_id | VARCHAR | 100 | NO | N/A | OID4VP request ID |
|  | state | VARCHAR | 100 | NO | N/A | OID4VP state |
|  | created_at | TIMESTAMP |  | NO | now() | created date |
