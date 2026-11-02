# Changelog

## [Unreleased] (v3.0.0)

### 🚀 New Features
- OID4VP verification
    - OpenDID VC, SD-JWT VC (`dc+sd-jwt`), and DID-based `mso_mdoc` credentials
    - Encrypted responses (`direct_post.jwt`, ECDH-ES / A256GCM)
    - DCQL scope mapping and OID4VP configuration
- Protocol-agnostic verification API (`/api/v2/initiate`, `/api/v2/status/{sessionId}`)
- Credential status check with IETF Token Status List
- VP History records the credential format and the error code of a failed submission
- LSS API key authentication (`lss` profile)
- Admin Console
    - OID4VP policy, DCQL scope mapping, and OID4VP configuration menus
    - Server Configuration (LSS API key)
    - Administrator password policy, admin ID change, and session management
    - Search and refresh on list pages
    - API call logs and audit logs

### 🔧 Changes
- OpenDID VP and ZKP verification now run through did-verifier-sdk-server 1.0.0
- Replaced local utilities with did-common-sdk-server 3.0.0
- Added did-oid4vp-sdk-server 3.0.0
- Admin Console menu paths moved under `/opendid`
- Upgraded Node.js to 22.23.4 for the admin console build
- Updated the Dockerfile Java image to eclipse-temurin 21 (jammy)

### 🐛 Bug Fixes
- ZKP proof verification checks the verification result, uses the nonce stored on the server, and rejects empty identifiers (did-zkp-sdk-server 2.0.1)
- Keep predicate `pType`/`pValue` in the ZKP proof request profile
- Fixed the Dockerfile to run the 3.0.0 jar
- Fixed a typo in the SSRVVRF00607 error message

## [Released] (v2.0.0)

## v2.0.0 (2025-05-30)
- Admin Console
    - ZKP policy Management
    - Adjusted some menu positions
- Added functionality for ZKP ProofRequest verification
- Support for Besu blockchain

### 🚀 New Features
- Admin Console
    - Verifier Management
    - VP Policy Management
    - VP History
    - Admin Management 
- Removed hardcoded logic and sample data from Verifier Server through Admin Console
  - Removed hardcoded YAML configuration values
  - Removed sample data previously embedded in Java source code

## v1.0.0 (2024-10-18)

### 🚀 New Features
- Verifier Server
- Verifying VP