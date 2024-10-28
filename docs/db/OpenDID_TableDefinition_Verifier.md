# Open DID Verifier Database Table Definition

- Date: 2024-09-04
- Version: v1.0.1

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

## 1. Overview

This document defines the structure of the database tables used in the Issuer server. It describes the field attributes, relationships, and data flow for each table, serving as essential reference material for system development and maintenance.

### 1.1 ERD

Access the [ERD](https://www.erdcloud.com/d/i6CfGGjhHg9mEcKKg) site to view the diagram, which visually represents the relationships between the tables in the Verifier server database, including key attributes, primary keys, and foreign key relationships.

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