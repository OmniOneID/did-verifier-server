# Open DID Verifier Database Table Definition

- Date: 2025-03-31
- Version: v1.0.1 (dev)

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
  - [2.9. payload](#29-payload)
  - [2.10. filter](#210-filter)
  - [2.11. policy_profile](#211-policy_profile)
  - [2.12. process](#212-process)
  - [2.13. verifier](#213-verifier)
  - [2.14. admin](#214-admin)

## 1. Overview

This document defines the structure of the database tables used in the Issuer server. It describes the field attributes, relationships, and data flow for each table, serving as essential reference material for system development and maintenance.

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

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | offer_id           | VARCHAR    | 40     | NO       | N/A      | Offer id                          |
|      | service            | VARCHAR    | 40     | NO       | N/A      | service id                        |
|      | device             | VARCHAR    | 40     | NO       | N/A      | device                            |
|      | payload            | LONGTEXT   |        | NO       | N/A      | payload                           |
|      | passcode           | VARCHAR    | 64     | YES      | N/A      | passcode                          |
|      | vp_policy_id       | VARCHAR    | 40     | NO       | N/A      | vp policy id                      |
|      | valid_until        | TIMESTAMP  |        | YES      | N/A      | offer valid until                 |
|      | created_at         | TIMESTAMP  |        | NO       | now()    | created date                      |
|      | updated_at         | TIMESTAMP  |        | YES      | N/A      | updated date                      |
| FK   | transaction_id     | BIGINT     |        | NO       | N/A      | transaction Key                   |

### 2.4. VP Submit

This table stores VP(Verifiable Presentation) submission information.

| Key  | Column Name        | Data Type  | Length | Nullable | Default  | Description                       |
|------|--------------------|------------|--------|----------|----------|-----------------------------------|
| PK   | id                 | BIGINT     |        | NO       | N/A      | id                                |
|      | vp                 | LONGTEXT   |        | NO       | N/A      | verfiable presentation            |
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

This table stores policy information used for verifying VP.

| key | column name       | data type | length | nullable | default | description       |
| --- | ----------------- | --------- | ------ | -------- | ------- | ----------------- |
| pk  | id                | varchar   | 255    | no       | n/a     | id                |
|     | service_id        | varchar   | 255    | yes      | n/a     | service id        |
|     | policy_profile_id | varchar   | 255    | yes      | n/a     | policy profile id |
|     | created_at        | timestamp |        | no       | now()   | created date      |
|     | updated_at        | timestamp |        | yes      | n/a     | updated date      |
|     | vp_service_id     | bigint    |        | no       | n/a     | vp service id     |
|     | vp_profile_id     | bigint    |        | no       | n/a     | vp profile id     |

### 2.9. payload

this table stores payload configuration for vp verification request.

| key | column name  | data type | length | nullable | default | description                     |
| --- | ------------ | --------- | ------ | -------- | ------- | ------------------------------- |
| pk  | id           | bigint    |        | no       | n/a     | id                              |
|     | device       | varchar   | 40     | no       | n/a     | device id                       |
|     | service      | varchar   | 40     | no       | n/a     | service id                      |
|     | endpoint     | varchar   | 100    | yes      | n/a     | endpoint url                    |
|     | locked       | boolean   |        | yes      | false   | whether the payload is locked   |
|     | mode         | varchar   | 40     | no       | n/a     | request mode                    |
|     | valid_second | tinyint   |        | no       | n/a     | payload valid time (in seconds) |
|     | created_at   | timestamp |        | no       | now()   | created date                    |
|     | updated_at   | timestamp |        | yes      | n/a     | updated date                    |

### 2.10. filter

this table stores vp filtering rules used to validate claims from verifiable credentials.

| key | column name     | data type | length | nullable | default | description                         |
| --- | --------------- | --------- | ------ | -------- | ------- | ----------------------------------- |
| pk  | filter_id       | bigint    |        | no       | n/a     | id                                  |
|     | name            | varchar   | 40     | no       | n/a     | filter name                         |
|     | id              | varchar   | 100    | no       | n/a     | filter identifier                   |
|     | type            | varchar   | 40     | no       | n/a     | vc schema format type               |
|     | display_claims  | varchar   | 100    | yes      | n/a     | claims to be displayed              |
|     | required_claims | varchar   | 100    | yes      | n/a     | claims required for verification    |
|     | present_all     | boolean   |        | yes      | n/a     | require all claims to be present    |
|     | value           | varchar   | 500    | yes      | n/a     | expected value of claim             |
|     | allowed_issuers | varchar   | 100    | yes      | n/a     | comma-separated list of issuer dids |
|     | created_at      | timestamp |        | no       | now()   | created date                        |
|     | updated_at      | timestamp |        | yes      | n/a     | updated date                        |


### 2.11. policy_profile

this table stores policy profiles that define rules for verifying vp against a specific filter and process.

| key | column name | data type | length | nullable | default | description         |
| --- | ----------- | --------- | ------ | -------- | ------- | ------------------- |
| pk  | id          | bigint    |        | no       | n/a     | id                  |
| pk  | process_id  | bigint    |        | no       | n/a     | process id          |
| pk  | filter_id   | bigint    |        | no       | n/a     | filter id           |
|     | profile id  | varchar   | 40     | no       | n/a     | profile identifier  |
|     | type        | varchar   | 40     | no       | n/a     | profile type        |
|     | title       | varchar   | 40     | no       | n/a     | profile title       |
|     | description | varchar   | 200    | yes      | n/a     | profile description |
|     | encoding    | varchar   | 40     | no       | n/a     | encoding format     |
|     | language    | varchar   | 40     | no       | n/a     | language of profile |
|     | created_at  | timestamp |        | no       | now()   | created date        |
|     | updated_at  | timestamp |        | yes      | n/a     | updated date        |

### 2.12. process

this table stores cryptographic process settings used for vp verification, including encryption and authentication configuration.

| key | column name | data type | length | nullable | default | description              |
| --- | ----------- | --------- | ------ | -------- | ------- | ------------------------ |
| pk  | id          | bigint    |        | no       | n/a     | id                       |
|     | endpoints   | varchar   | 100    | yes      | n/a     | api endpoint url         |
|     | curve       | varchar   | 40     | no       | n/a     | elliptic curve algorithm |
|     | public_key  | varchar   | 40     | no       | n/a     | public key               |
|     | cipher      | varchar   | 40     | no       | n/a     | cipher algorithm         |
|     | padding     | varchar   | 40     | no       | n/a     | padding type             |
|     | auth_type   | varchar   | 40     | no       | n/a     | auth type                |
|     | created_at  | timestamp |        | no       | now()   | created date             |
|     | updated_at  | timestamp |        | yes      | n/a     | updated date             |

### 2.13. verifier

this table stores information about verifiers that receive and validate verifiable presentations.

| key | column name     | data type | length | nullable | default | description              |
| --- | --------------- | --------- | ------ | -------- | ------- | ------------------------ |
| pk  | id              | bigint    |        | no       | n/a     | id                       |
|     | did             | varchar   | 200    | no       | n/a     | did                      |
|     | name            | varchar   | 200    | no       | n/a     | name                     |
|     | status          | varchar   | 50     | no       | n/a     | status                   |
|     | server_url      | varchar   | 2000   | yes      | n/a     | verifier server url      |
|     | certificate_url | varchar   | 2000   | yes      | n/a     | certificate endpoint url |
|     | created_at      | timestamp |        | no       | now()   | created date             |
|     | updated_at      | timestamp |        | yes      | n/a     | updated date             |

### 2.14. admin

this table stores administrator account information for managing the verifier system.

| key  | column name            | data type  | length | nullable | default  | description                                |
|------|------------------------|------------|--------|----------|----------|--------------------------------------------|
| pk   | id                     | bigint     |        | no       | n/a      | id                                   |
|      | login_id               | varchar    | 50     | no       | n/a      | administrator login id                     |
|      | login_password         | varchar    | 64     | no       | n/a      | hashed login password                      |
|      | name                   | varchar    | 200    | no       | n/a      | administrator name                         |
|      | email                  | varchar    | 100    | yes      | n/a      | email address                              |
|      | email_verified         | boolean    |        | yes      | false    | whether email is verified                  |
|      | require_password_reset | boolean    |        | no       | true     | force password reset on next login         |
|      | role                   | varchar    | 50     | yes      | n/a      | admin role                                 |
|      | created_by             | varchar    | 50     | no       | n/a      | creator's login id                         |
|      | created_at             | timestamp  |        | no       | now()    | created date                               |
|      | updated_at             | timestamp  |        | yes      | n/a      | updated date                               |
