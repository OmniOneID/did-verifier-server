# Open DID Verifier Database Table Definition

- Date: 2025-05-30
- Version: v2.0.0

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
  - [2.11. Policy Profile](#211-policy_profile)
  - [2.12. Process](#212-process)
  - [2.13. Verifier](#213-verifier)
  - [2.14. Admin](#214-admin)
  - [2.15. ZKP Proof Request](#215-zkp-proof-request)
  - [2.16. ZKP Policy Profile](#216-zkp-policy-profile)

## 1. Overview

This document defines the structure of the database tables used in the Verifier server. It describes the field attributes, relationships, and data flow for each table, serving as essential reference material for system development and maintenance.

### 1.1 ERD

Access the [ERD](https://www.erdcloud.com/d/refvZerYJ5mc5FKaa) site to view the diagram, which visually represents the relationships between the tables in the Verifier server database, including key attributes, primary keys, and foreign key relationships.

## 2. Table Definition

### 2.1. Transaction

This table stores transaction information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | tx_id              | VARCHAR    | 40     | NO       | N/A      | transaction id                    |
|      | type               | VARCHAR    | 50     | NO       | N/A      | transaction type                  |
|      | status             | VARCHAR    | 50     | NO       | N/A      | transaction status                |
|      | expired_at         | TIMESTAMP  |        | NO       | N/A      | expiration date                   |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | updated date                      |

### 2.2. Sub Transaction

This table stores sub-transaction information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | step               | TINYINT    |        | NO       | N/A      | step                              |
|      | type               | VARCHAR    | 50     | NO       | N/A      | transaction type                  |
|      | status             | VARCHAR    | 50     | NO       | N/A      | status                            |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | updated date                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | transaction key                   |

### 2.3. VP Offer

This table stores VP(Verifiable Presentation) offer information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default      | Description                       |
|------|--------------------|------------|--------|----------|--------------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A          | id                                |
|      | offer_id           | VARCHAR    | 40     | NO       | N/A          | Offer id                          |
|      | service            | VARCHAR    | 40     | NO       | N/A          | service id                        |
|      | device             | VARCHAR    | 40     | NO       | N/A          | device                            |
|      | payload            | LONGTEXT   |        | NO       | N/A          | payload                           |
|      | passcode           | VARCHAR    | 64     | YES      | N/A          | passcode                          |
|      | vp_policy_id       | VARCHAR    | 40     | NO       | N/A          | vp policy id                      |
|      | offer_type         | VARCHAR    | 40     | NO       | VerifyOffer  | offer type                        |
|      | valid_until        | TIMESTAMP  |        | YES      | N/A          | offer valid until                 |
|      | created_at         | TIMESTAMP  |        | NO       | now()        | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A          | updated date                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A          | transaction Key                   |

### 2.4. VP Submit

This table stores VP(Verifiable Presentation) submission information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | vp                 | LONGTEXT   |        | NO       | N/A      | verfiable presentation            |
|      | vp_did             | VARCHAR    | 40     | YES      | N/A      | verifiable presentation DID       |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | updated date                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | transaction key                   |

### 2.5. VP Profile

This table stores VP(Verifiable Presentation) profile information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | profile_id         | VARCHAR    | 40     | NO       | N/A      | vp profile id                     |
|      | vp_profile         | LONGTEXT   |        | NO       | N/A      | vp profile                        |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | updated date                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | transaction key                   |

### 2.6. E2E

This table stores E2E (End-to-End Encryption) information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | session_key        | VARCHAR    | 100    | NO       | N/A      | session key                       |
|      | nonce              | VARCHAR    | 100    | NO       | N/A      | nonce                             |
|      | curve              | VARCHAR    | 20     | NO       | N/A      | curve                             |
|      | cipher             | VARCHAR    | 20     | NO       | N/A      | cipher type                       |
|      | padding            | VARCHAR    | 20     | NO       | N/A      | padding                           |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | updated date                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | transaction key                   |

### 2.7. Certificate VC

This table stores Certificate VC(Verifiable Credential) information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | vc                 | TEXT       |        | NO       | N/A      | certificate VC contents (json)    |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | updated date                      |

### 2.8. Policy

This table stores policy information used for verifying VP and ZKP.

| Key | Column Name        | Data Type | Length | Nullable | Default | Description                                |
|-----|--------------------|-----------|---------|-----------|---------|--------------------------------------------|
| PK  | id                 | VARCHAR   | 255    | NO        | N/A     | id                                         |
|     | policy_id          | VARCHAR   | 40     | NO        | N/A     | policy identifier                          |
|     | payload_id         | VARCHAR   | 40     | NO        | N/A     | payload identifier                         |
|     | policy_profile_id  | VARCHAR   | 40     | NO        | N/A     | policy profile identifier                  |
|     | policy_title       | VARCHAR   | 255    | NO        | N/A     | policy title                               |
|     | policy_type        | VARCHAR   | 40     | NO        | VP      | policy type (ZKP, VP)                      |
|     | created_at         | TIMESTAMP |        | NO        | now()   | created date                               |
|     | updated_at         | TIMESTAMP |        | YES       | N/A     | updated date                               |

### 2.9. Payload

This table stores payload configuration for VP verification request.

| Key | Column Name   | Data Type | Length | Nullable | Default    | Description                                |
|-----|---------------|-----------|---------|-----------|-----------|--------------------------------------------|
| PK  | id            | BIGINT    |        | NO        | N/A       | id                                         |
|     | payload_id    | VARCHAR   | 40     | NO        | N/A       | payload identifier                         |
|     | device        | VARCHAR   | 40     | NO        | N/A       | device id                                  |
|     | service       | VARCHAR   | 40     | NO        | N/A       | service id                                 |
|     | endpoint      | VARCHAR   | 100    | YES       | N/A       | endpoint url                               |
|     | locked        | BOOLEAN   |        | YES       | false     | whether the payload is locked              |
|     | mode          | VARCHAR   | 40     | NO        | N/A       | request mode                               |
|     | valid_second  | TINYINT   |        | NO        | N/A       | payload valid time (in seconds)            |
|     | offer_type    | VARCHAR   | 40     | NO        | Offer Type| offer type                                 |
|     | created_at    | TIMESTAMP |        | NO        | now()     | created date                               |
|     | updated_at    | TIMESTAMP |        | YES       | N/A       | updated date                               |

### 2.10. Filter

This table stores VP filtering rules used to validate claims from verifiable credentials.

| Key | Column Name     | Data Type | Length | Nullable | Default | Description                              |
|-----|-----------------|-----------|---------|----------|---------|------------------------------------------|
| PK  | filter_id       | BIGINT    |        | NO       | N/A     | filter identifier                        |
|     | name            | VARCHAR   | 40     | NO       | N/A     | filter name                              |
|     | id              | VARCHAR   | 100    | NO       | N/A     | filter internal identifier               |
|     | type            | VARCHAR   | 40     | NO       | N/A     | vc schema format type                    |
|     | display_claims  | VARCHAR   | 100    | YES      | N/A     | claims to be displayed                   |
|     | required_claims | VARCHAR   | 100    | YES      | N/A     | claims required for verification         |
|     | present_all     | BOOLEAN   |        | YES      | N/A     | require all claims to be present         |
|     | value           | VARCHAR   | 500    | YES      | N/A     | expected value of claim                  |
|     | allowed_issuers | VARCHAR   | 100    | YES      | N/A     | comma-separated list of issuer dids      |
|     | created_at      | TIMESTAMP |        | NO       | now()   | created date                             |
|     | updated_at      | TIMESTAMP |        | YES      | N/A     | updated date                             |

### 2.11. Policy Profile

This table stores policy profiles that define rules for verifying VP against a specific filter and process.

| Key | Column Name | Data Type | Length | Nullable | Default | Description         |
|-----|-------------|-----------|---------|----------|---------|---------------------|
| PK  | id          | BIGINT    |        | NO       | N/A     | id                  |
| PK  | process_id  | BIGINT    |        | NO       | N/A     | process id          |
| PK  | filter_id   | BIGINT    |        | NO       | N/A     | filter id           |
|     | profile_id  | VARCHAR   | 40     | NO       | N/A     | profile identifier  |
|     | type        | VARCHAR   | 40     | NO       | N/A     | profile type        |
|     | title       | VARCHAR   | 40     | NO       | N/A     | profile title       |
|     | description | VARCHAR   | 200    | YES      | N/A     | profile description |
|     | encoding    | VARCHAR   | 40     | NO       | N/A     | encoding format     |
|     | language    | VARCHAR   | 40     | NO       | N/A     | language of profile |
|     | created_at  | TIMESTAMP |        | NO       | now()   | created date        |
|     | updated_at  | TIMESTAMP |        | YES      | N/A     | updated date        |

### 2.12. Process

This table stores cryptographic process settings used for VP verification, including encryption and authentication configuration.

| Key | Column Name | Data Type | Length | Nullable | Default | Description              |
|-----|-------------|-----------|---------|----------|---------|--------------------------|
| PK  | id          | BIGINT    |        | NO       | N/A     | id                       |
|     | endpoints   | VARCHAR   | 100    | YES      | N/A     | api endpoint url         |
|     | curve       | VARCHAR   | 40     | NO       | N/A     | elliptic curve algorithm |
|     | public_key  | VARCHAR   | 40     | NO       | N/A     | public key               |
|     | cipher      | VARCHAR   | 40     | NO       | N/A     | cipher algorithm         |
|     | padding     | VARCHAR   | 40     | NO       | N/A     | padding type             |
|     | auth_type   | VARCHAR   | 40     | NO       | N/A     | auth type                |
|     | created_at  | TIMESTAMP |        | NO       | now()   | created date             |
|     | updated_at  | TIMESTAMP |        | YES      | N/A     | updated date             |

### 2.13. Verifier

This table stores information about verifiers that receive and validate verifiable presentations.

| Key | Column Name     | Data Type | Length | Nullable | Default | Description              |
|-----|-----------------|-----------|---------|----------|---------|--------------------------|
| PK  | id              | BIGINT    |        | NO       | N/A     | id                       |
|     | did             | VARCHAR   | 200    | NO       | N/A     | did                      |
|     | name            | VARCHAR   | 200    | NO       | N/A     | name                     |
|     | status          | VARCHAR   | 50     | NO       | N/A     | status                   |
|     | server_url      | VARCHAR   | 2000   | YES      | N/A     | verifier server url      |
|     | certificate_url | VARCHAR   | 2000   | YES      | N/A     | certificate endpoint url |
|     | created_at      | TIMESTAMP |        | NO       | now()   | created date             |
|     | updated_at      | TIMESTAMP |        | YES      | N/A     | updated date             |

### 2.14. Admin

This table stores administrator account information for managing the verifier system.

| Key  | Column Name            | Data Type  | Length | Nullable | Default  | Description                                |
|------|------------------------|------------|--------|----------|----------|--------------------------------------------|
| PK   | id                     | BIGINT     |        | NO       | N/A      | id                                         |
|      | login_id               | VARCHAR    | 50     | NO       | N/A      | administrator login id                     |
|      | login_password         | VARCHAR    | 64     | NO       | N/A      | hashed login password                      |
|      | name                   | VARCHAR    | 200    | NO       | N/A      | administrator name                         |
|      | email                  | VARCHAR    | 100    | YES      | N/A      | email address                              |
|      | email_verified         | BOOLEAN    |        | YES      | false    | whether email is verified                  |
|      | require_password_reset | BOOLEAN    |        | NO       | true     | force password reset on next login        |
|      | role                   | VARCHAR    | 50     | YES      | N/A      | admin role                                 |
|      | created_by             | VARCHAR    | 50     | NO       | N/A      | creator's login id                         |
|      | created_at             | TIMESTAMP  |        | NO       | now()    | created date                               |
|      | updated_at             | TIMESTAMP  |        | YES      | N/A      | updated date                               |

### 2.15. ZKP Proof Request

This table stores ZKP (Zero-Knowledge Proof) proof request information for ZKP-based verification.

| Key | Column Name           | Data Type | Length | Nullable | Default | Description                                    |
|-----|-----------------------|-----------|---------|----------|---------|------------------------------------------------|
| PK  | id                    | BIGINT    |        | NO       | N/A     | id                                             |
|     | name                  | VARCHAR   | 40     | NO       | N/A     | proof request name                             |
|     | version               | VARCHAR   | 10     | NO       | N/A     | proof request version                          |
|     | requested_attributes  | TEXT      |        | YES      | N/A     | requested attributes in JSON format            |
|     | requested_predicates  | TEXT      |        | YES      | N/A     | requested predicates in JSON format            |
|     | curve                 | VARCHAR   | 40     | NO       | N/A     | elliptic curve algorithm                       |
|     | cipher                | VARCHAR   | 40     | NO       | N/A     | cipher algorithm                               |
|     | padding               | VARCHAR   | 40     | NO       | N/A     | padding type                                   |
|     | created_at            | TIMESTAMP |        | NO       | now()   | created date                                   |
|     | updated_at            | TIMESTAMP |        | YES      | N/A     | updated date                                   |

### 2.16. ZKP Policy Profile

This table stores ZKP policy profiles that define rules for ZKP-based verification.

| Key | Column Name           | Data Type | Length | Nullable | Default | Description                    |
|-----|-----------------------|-----------|---------|----------|---------|--------------------------------|
| PK  | id                    | BIGINT    |        | NO       | N/A     | id                             |
|     | profile_id            | VARCHAR   | 40     | NO       | N/A     | ZKP profile identifier         |
|     | type                  | VARCHAR   | 40     | NO       | N/A     | profile type                   |
|     | title                 | VARCHAR   | 40     | NO       | N/A     | profile title                  |
|     | description           | VARCHAR   | 200    | YES      | N/A     | profile description            |
|     | encoding              | VARCHAR   | 40     | NO       | N/A     | encoding format                |
|     | language              | VARCHAR   | 40     | NO       | N/A     | language of profile            |
|     | created_at            | TIMESTAMP |        | NO       | now()   | created date                   |
|     | updated_at            | TIMESTAMP |        | YES      | N/A     | updated date                   |
| FK  | zkp_proof_request_id  | BIGINT    |        | NO       | N/A     | reference to zkp_proof_request |
