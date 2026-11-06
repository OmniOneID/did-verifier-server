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

Open DID Verifier Admin Operation Guide
==

- Date: 2026-11-06
- Version: v3.0.0

Revision History
==
| Version | Date       | Changes                                    |
| ------- | ---------- | ------------------------------------------ |
| v1.0.0  | 2025-03-31 | Initial creation                          |
| v1.0.1  | 2025-04-25 | Added `3.1. Verifier Registration` section |
| v2.0.0  | 2025-05-30 | Added ZKP verification-related menus and functional changes |
| v3.0.0  | 2026-11-06 | Updated navigation, OID4VP, API logs, settings, and screenshots; added example data screenshots and detailed OID4VP, log, and settings descriptions |

Table of Contents
==
- [Revision History](#revision-history)
- [1. Table of Contents](#1-table-of-contents)
  - [1.1. Overview](#11-overview)
  - [1.2. Admin Console Definition](#12-admin-console-definition)
- [2. Basic Manual](#2-basic-manual)
  - [2.1. Login](#21-login)
  - [2.2. Main Screen Layout](#22-main-screen-layout)
  - [2.3. Menu Structure](#23-menu-structure)
    - [2.3.1. Verifier Unregistered State](#231-verifier-unregistered-state)
    - [2.3.2. Verifier Registration Complete State](#232-verifier-registration-complete-state)
    - [2.3.3. VP Policy Management Sub-menus](#233-vp-policy-management-sub-menus)
    - [2.3.4. ZKP Policy Management Sub-menus](#234-zkp-policy-management-sub-menus)
    - [2.3.5. Recommended Setup Order](#235-recommended-setup-order)
  - [2.4. Password Change Management](#24-password-change-management)
- [3. Detailed Feature Manual](#3-detailed-feature-manual)
  - [3.1. Verifier Management](#31-verifier-management)
    - [3.1.1. Verifier Registration Process](#311-verifier-registration-process)
    - [3.1.2. Registered Verifier Information Management](#312-registered-verifier-information-management)
    - [3.1.3. Troubleshooting Guide](#313-troubleshooting-guide)
  - [3.2. Service Management](#32-service-management)
    - [3.2.1. Service Management Overview](#321-service-management-overview)
    - [3.2.2. Service Registration / Modification](#322-service-registration--modification)
  - [3.3. VP Policy Management](#33-vp-policy-management)
    - [3.3.1. Filter Management](#331-filter-management)
      - [3.3.1.1. Filter Management Screen Layout](#3311-filter-management-screen-layout)
      - [3.3.1.2. Filter Registration / Modification](#3312-filter-registration--modification)
    - [3.3.2. Process Management](#332-process-management)
      - [3.3.2.1. Process Management Screen Layout](#3321-process-management-screen-layout)
      - [3.3.2.2. Process Registration / Modification](#3322-process-registration--modification)
    - [3.3.3. Profile Management](#333-profile-management)
      - [3.3.3.1. Profile Management Screen Layout](#3331-profile-management-screen-layout)
      - [3.3.3.2. Profile Registration / Modification](#3332-profile-registration--modification)
    - [3.3.4. Policy Management](#334-policy-management)
      - [3.3.4.1. Policy Management Screen Layout](#3341-policy-management-screen-layout)
      - [3.3.4.2. Policy Registration](#3342-policy-registration)
  - [3.4. ZKP Policy Management](#34-zkp-policy-management)
    - [3.4.1. Proof Request Configuration](#341-proof-request-configuration)
      - [3.4.1.1. Proof Request Register](#3411-proof-request-register)
    - [3.4.2. ZKP Profile Management](#342-zkp-profile-management)
      - [3.4.2.1. ZKP Profile Register](#3421-zkp-profile-register)
    - [3.4.3. ZKP Policy Management](#343-zkp-policy-management)
      - [3.4.3.1. ZKP Policy Register](#3431-zkp-policy-register)
  - [3.5. VP History](#35-vp-history)
  - [3.6. Admin Management](#36-admin-management)
    - [3.6.1. Admin List View](#361-admin-list-view)
    - [3.6.2. Admin Registration](#362-admin-registration)
  - [3.7. OID4VP Management](#37-oid4vp-management)
    - [3.7.1. Configuration](#371-configuration)
    - [3.7.2. DCQL Scope Management](#372-dcql-scope-management)
    - [3.7.3. Policy Management](#373-policy-management)
  - [3.8. Log Management](#38-log-management)
    - [3.8.1. API Log](#381-api-log)
    - [3.8.2. Audit API Log](#382-audit-api-log)
  - [3.9. Server Configuration](#39-server-configuration)
  - [3.10. Password Policy Settings](#310-password-policy-settings)

## 1.1. Overview

This document provides a guide for the installation and operation of the Open DID Verifier Admin Console. It explains the basic manual and detailed feature manual of the Admin Console step by step, helping users to use it efficiently.

For a complete installation guide for OpenDID, please refer to the [Open DID Installation Guide].

<br/>

## 1.2. Admin Console Definition

The Verifier Admin Console is a web-based management tool for managing Verifier servers in the Open DID system. Through this tool, administrators can set up Verifiable Presentation (VP) and ZKP Credential verification policies, monitor verification requests and results, and manage system settings.

The Admin Console provides the following key features:

- **Verifier Basic Information Management**
  - Verifier server registration and status checking
- **Service(Payload) Basic Information Management**
  - Service(payload) registration and item configuration
- **VP Policy Item Management**
  - Profile Filter registration and management
  - Profile Process registration and management
  - VP Profile setup
  - VP Policy setup
- **ZKP Policy Item Management**
  - Proof Request registration and management
  - ZKP Profile registration and management
  - ZKP Policy setup
- **OID4VP Management**
  - Client configuration, DCQL scopes, and verification policies
- **API Log Management**
  - Agent API calls and administrator action history
- **VP History**
  - VP verification history checking
- **Server and Password Policy Settings**
  - External server integration and administrator password rules
- **Administrator Account Management**
  - Admin Console account management

# 2. Basic Manual

This chapter provides guidance on the basic usage of the Open DID Verifier Admin Console.

## 2.1. Login

To access the Admin Console, follow these steps:

1. Open a web browser and access the Verifier Admin Console URL.

   ```
   http://<verifier_domain>:<port>
   ```

2. On the login screen, enter the administrator account email and password.
   - Default administrator account: <admin@opendid.omnione.net>
   - Initial password: password (password change required on first login)

3. Click the 'Login' button.

> **Note**: For security reasons, a password change is required on first login.

<br/>

## 2.2. Main Screen Layout

The main screen displayed after login consists of the following elements:

<img src="./images/api-log-api-example.png" width="800"/>

> Screen rendered with example data.

| Number | Area | Description |
|--------|------|-------------|
| 1 | Settings Button | Clicking the 'SETTING' button navigates to a screen where you can change the password of the currently logged-in account. |
| 2 | Content Header | The title of the currently selected menu is displayed. The page name is shown for each menu. |
| 3 | Sidebar Menu | Lists menus for accessing major functions including Verifier Management. |
| 4 | User Information Area | Shows the email of the currently logged-in administrator (<admin@opendid.omnione.net>) and logout button. |

<br/>

## 2.3. Menu Structure

The sidebar menu of the Verifier Admin Console **differs in screen configuration depending on the Verifier registration status**. This reflects the phased setup procedure of the OpenDID system, guiding users to configure the system sequentially.

### 2.3.1. Verifier Unregistered State

In the initial state where the Verifier server is not yet registered, only the **Verifier Registration** item is displayed alone in the menu.

<img src="./images/verifier-menu-before-registration.jpg" width="300"/>

> **Important**: In the OpenDID system, the Verifier is a core element that forms the foundation of all verification policies. VP and ZKP verification policies can only be set up after Verifier registration is complete.

### 2.3.2. Verifier Registration Complete State

Once Verifier registration is complete, all management functions are activated, and the sidebar menu is configured as follows:

<img src="./images/menu_structure.jpg" width="800"/>

| Menu | Submenus | Function |
|------|----------|----------|
| Verifier Management | — | View registered Verifier information |
| Service Management | — | Manage verification services |
| OpenDID > VP | Filter, Process, Profile, Policy Management | Configure VP verification policies |
| OpenDID > ZKP | Proof Request, Profile, Policy Management | Configure ZKP verification policies |
| OID4VP | Configuration, DCQL Scope Management, Policy Management | Manage OID4VP settings and policies |
| VP History | — | Review VP submissions and verification history |
| Log Management | API Log, Audit API Log | Review Agent API calls and admin actions |
| Server Configuration | — | Review and manage server integration settings |
| Admin Management | Admin Management, Password Policy Settings | Manage admin accounts and password rules |

> `OpenDID` VP/ZKP, `OID4VP`, `Log Management`, and `Admin Management` expand to show submenus. Before registration, only `Verifier Registration` appears.

### 2.3.3. VP Policy Management Sub-menus

Expand `OpenDID > VP` to see these submenus:

| Sub-menu | Function Description | Setup Order |
|----------|---------------------|-------------|
| **Filter Management** | Define VC schema and claim conditions required for VP verification. | 2-1 |
| **Process Management** | Set encryption and authentication methods to be used during VP submission process. | 2-2 |
| **Profile Management** | Create VP verification profiles by combining Filter and Process. | 2-3 |
| **Policy Management** | Complete final VP verification policy by connecting Service and Profile. | 2-4 |

### 2.3.4. ZKP Policy Management Sub-menus

Expand `OpenDID > ZKP` to see these submenus:

| Sub-menu | Function Description | Setup Order |
|----------|---------------------|-------------|
| **Proof Request Configuration** | Define attributes and predicates required for ZKP proof. | 3-1 |
| **ZKP Profile Management** | Create ZKP verification profiles including Proof Request. | 3-2 |
| **ZKP Policy Management** | Complete final ZKP verification policy by connecting Service and ZKP Profile. | 3-3 |

### 2.3.5. Recommended Setup Order

For efficient system configuration, we recommend setting up in the following order:

#### **Step 1: Basic Setup**
1. **Verifier Registration** - Register Verifier server and generate DID Document
2. **Service Management** - Register basic verification service information

#### **Step 2: VP Policy Setup** (For general VP verification)
1. **Filter Management** - Define VC schema and claim conditions
2. **Process Management** - Set encryption and authentication
3. **Profile Management** - Combine Filter + Process
4. **Policy Management** - Connect Service + Profile

#### **Step 3: ZKP Policy Setup** (For Zero-Knowledge Proof)
1. **Proof Request Configuration** - Define ZKP proof requirements
2. **ZKP Profile Management** - Create ZKP profile
3. **ZKP Policy Management** - Connect Service + ZKP Profile

#### **Step 4: Operation and Management**
- **VP History** - Monitor verification history
- **Admin Management** - Manage administrator accounts

> **Note**: VP and ZKP are different verification methods, so you can set up only the policies needed according to your service requirements.
> - **VP**: General identity verification (e.g., ID card, degree certificate)
> - **ZKP**: Privacy-important verification (e.g., age proof, income range proof)

<br/>

## 2.4. Password Change Management

User password changes can be performed through the following steps:

1. Click the 'SETTING' button in the header area.
2. Select 'Password Change' from the settings menu.
3. In the password change screen:
   - Enter current password
   - Enter new password
   - Confirm new password
4. Click the 'Save' button to apply changes.

> **Note**: Password must be between 8 and 64 characters.

<br/>

# 3. Detailed Feature Manual

> Screens after registration show the current sidebar with example data. Actual data varies by environment.

This chapter provides detailed usage instructions for the main features of the Open DID Verifier Admin Console.

## 3.1. Verifier Management

Verifier Management is a core function responsible for registering and managing Verifier servers in the OpenDID system.

The Verifier is the entity that verifies VP (Verifiable Presentation) and ZKP Proof submitted by users, and is an important component that forms the foundation of all verification policies. Verifier registration is performed **only once** during initial system setup, and afterwards, you can view and manage the registered information.

### 3.1.1. Verifier Registration Process

Verifier registration is a step-by-step procedure to safely participate new verifiers in the OpenDID ecosystem. It proceeds through a total of **3 sequential steps**.

#### **Overall Registration Flow**
```
Step 1: Enter Verifier basic information and connection test
     ↓
Step 2: Generate DID Document and request TA registration
     ↓ (2-1: Generate → 2-2: Request → 2-3: Check approval)
     ↓
Step 3: Entity registration and membership certificate issuance
     ↓
Registration Complete: Move to Verifier Management menu
```

---

#### **Step 1 - Enter Verifier Basic Information**

In the first step, enter the basic information of the Verifier server and check actual connectivity.

<img src="./images/verifier-registration.png" width="800"/>

| Input Item | Description | Input Example | Required |
|------------|-------------|---------------|----------|
| **Name** | Enter the name of the Verifier server. Use 3 to 20 English characters. | `ExampleVerifier` | ✅ Required |
| **Verifier URL** | Enter the access URL of the Verifier server (up to 200 characters). The default port is `8092`. | `https://example.org/verifier` | ✅ Required |

##### **Connection Test Function**
- Click the **Test Connection** button to check if actual connection is possible with the entered URL.
- The **NEXT** button is activated to proceed to the next step only when connection is successful.
- If connection fails, recheck the URL and verify that the Verifier server is running properly.

> **Note**: The Verifier URL format must be in the form `http://<IP or domain>:<port>/verifier`.

---

#### **Step 2 - DID Document Registration**

The second step is the process of registering the Verifier's identity on the blockchain, consisting of **3 detailed steps**.

> **Important**: Once created, DID Documents **cannot be modified or re-registered**. Please proceed carefully.

##### **Step 2-1: DID Document Generation**

Automatically generates a unique DID Document for the Verifier server.

<img src="./images/register-did-document-step2.png" width="800"/>

| Button | Function | Result |
|--------|----------|--------|
| **GENERATE** | Automatically generate Verifier DID Document | Generate JSON document including DID, public key, and service endpoint |

- Upon successful generation, a green message appears and the Step 2-2 area is automatically displayed.
- The generated DID Document includes the following information:
  - **DID**: Distributed identity identifier in the form `did:omn:verifier:<unique identifier>`
  - **Public Key**: Cryptographic key information for verification
  - **Service Endpoint**: Access point information for the Verifier server

##### **Step 2-2: TA Admin Registration Request**

Request registration of the generated DID Document to the Trust Authority (TA).

<img src="./images/register-did-document-step2-1.png" width="800"/>

| Button | Function | Description |
|--------|----------|-------------|
| **REQUEST** | Send DID registration request to TA Admin | Create a request that requires manual approval from TA administrator |

- Upon successful request, a green message appears and the Step 2-3 area is automatically displayed.
- After this step, you need to wait for approval from the TA administrator.

##### **Step 2-3: Check Approval Status**

Check whether the TA administrator has approved the DID Document registration request.

<img src="./images/check-approval.png" width="800"/>

| Button | Function | Result by Status |
|--------|----------|------------------|
| **CHECK** | Query TA approval status | **Pending**: Pending notice popup<br>**Approved**: Green message + NEXT button activated<br>**Rejected**: Rejection notice popup, then returns to the first screen |

> **TA Administrator approval process** is an essential process for the security of the OpenDID ecosystem. Please check the status regularly until approval is complete.

---

#### **Step 3 - Entity Registration and Membership Certificate Issuance**

In the final step, register the Verifier as an official member (Entity) of the OpenDID ecosystem and receive a membership certificate.

<img src="./images/verifier-regstration-step3.png" width="800"/>

This step automatically executes **TA Protocol P120 (Entity Registration Protocol)** to perform the following processes:

1. **Entity Registration Request**: Register Verifier as Entity on TA server
2. **Certificate Issuance**: Generate and receive membership certificate (Certificate VC)
3. **Wallet Key Management**: Securely store Verifier's private/public key pair

| Button | Function | Processing Content |
|--------|----------|-------------------|
| **REQUEST** | Execute Entity registration protocol | Automatically execute TA Protocol P120 |
| **FINISH** | Complete registration and move to main screen | Switch to Verifier Management screen |

> The membership certificate (Certificate VC) is a digital certificate that proves the Verifier is a trustworthy verifier. It is used to prove the Verifier's identity in all subsequent verification processes.

---

### 3.1.2. Registered Verifier Information Management

Once Verifier registration is complete, you can view detailed information of the registered Verifier in the **Verifier Management** menu.

<img src="./images/verifier_management.jpg" width="800"/>

#### **Information View Screen Layout**

| Item | Display Content | Description |
|------|----------------|-------------|
| **DID** | `did:omn:verifier:<identifier>` | Unique distributed identity identifier of the Verifier |
| **Name** | Name entered during registration | Verifier name displayed to users during VP verification |
| **Status** | `ACTIVATE` / `DEACTIVATE` | Current activation status of the Verifier |
| **URL** | Verifier service base URL | Access point for processing verification requests |
| **Certificate URL** | Membership certificate view URL | Link to access Certificate VC |
| **Registered At** | Registration completion date/time | Date and time when Verifier was registered in the system |

#### **DID Document Detailed View**

Click the **VIEW DID DOCUMENT** button to view the complete DID Document registered on the blockchain.

**Viewable Information:**
- **@context**: JSON-LD context of the DID Document
- **id**: Verifier DID identifier
- **controller**: Information about the entity controlling the DID
- **created**: DID Document creation date/time
- **updated**: Last update date/time
- **verificationMethod**: Public key information for signature verification
- **service**: Verifier service endpoint information

> **Note**: DID Documents are permanently recorded on the blockchain, making them tamper-proof and verifiable by all participants in the OpenDID ecosystem.

---

### 3.1.3. Troubleshooting Guide

#### **Connection Test Failure**
- Check if the Verifier server is running properly
- Verify that ports are not blocked by firewall settings
- Confirm URL format is correct (`http://IP:PORT/verifier`)

#### **DID Registration Approval Delay**
- Contact TA administrator about approval request status
- Periodically click CHECK button to verify status
- If the request is rejected, ask the TA administrator for the reason, resolve the problem, and try again

#### **Entity Registration Failure**
- Check network connection status
- Verify TA server status
- Check Verifier server logs for error messages

> **Important**: If problems occur during the Verifier registration process, you may need to start over from the beginning. Please perform each step carefully.

## 3.2. Service Management

Service Management is a function that defines and manages service information to be used in VP and ZKP verification policies.

Service is **a core component that defines the purpose and method of verification requests**, telling users what type of verification is required and providing endpoints to process results after verification completion. All VP and ZKP policies must be connected to one service, so it must be configured first before setting up policies.

### 3.2.1. Service Management Overview

The Service Management menu allows you to manage the entire lifecycle of services.

<img src="./images/service_configuration.jpg" width="800"/>

#### **Screen Components**

| Number | Component | Function Description | Permission |
|--------|-----------|---------------------|------------|
| 1 | **REGISTER Button** | Navigate to page for registering new service. | All administrators |
| 2 | **UPDATE Button** | Modify information of selected service. | All administrators |
| 3 | **DELETE Button** | Delete selected service. | All administrators |
| 4 | **Service List** | Display summary information of all registered services. | View only |
| 5 | **Pagination** | Navigate large service lists by pages. | View only |

#### **Service List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Service Name** | Service unique name | Link that navigates to detail page when clicked |
| **Device** | Supported device type | PC, Mobile, Tablet, etc. |
| **Lock Status** | Service lock status | `locked` / `unlocked` (currently only `unlocked` supported) |
| **Submission Mode** | VP submission method | `Direct` / `Indirect` / `Proxy` |
| **Verification Type** | Verification type | `VP` (general verification) / `ZKP` (zero-knowledge proof) |
| **Policy Count** | Number of connected policies | Number of policies using this service |

> **Important**: Services with Policy Count of 1 or more cannot be deleted. To delete, you must first remove all connected policies.

---

### 3.2.2. Service Registration / Modification

Click the **REGISTER** button to register a new service and navigate to the service registration page.
The same screen layout is used when **UPDATE** modifying services.

<img src="./images/service_registration.jpg" width="800"/>

#### **Required Input Items**

| Item | Description | Input Example | Constraints |
|------|-------------|---------------|-------------|
| **Service*** | Unique name of the service | `IDCardVerification`, `AgeProof` | Maximum 50 characters |
| **Device*** | Supported device type | `PC`, `Mobile`, `Tablet` | Free text input, maximum 50 characters |
| **Lock Status*** | Service lock status | `Unlocked` | Currently only `Unlocked` selectable |
| **Submission Mode*** | VP submission method selection | `Direct` | Currently only `Direct` selectable |
| **Verification Type*** | Verification type selection | `VP`, `ZKP` | Select according to verification method to use |
| **Valid Seconds*** | Validity period of the verification request in seconds | `180` | 1 to 86400, default 180 |

> **Note**: * indicates required input items.

#### **Submission Mode Details**

| Mode | Description | When to Use |
|------|-------------|-------------|
| **Direct** | User submits VP directly to Verifier | When real-time verification is needed | 
| **Indirect** | VP delivery through intermediate server | In network-constrained environments |
| **Proxy** | Indirect delivery through proxy server | Complex network configurations | 

> **Note**: Currently only `Direct` can be selected on this screen.

#### **Verification Type Details**

| Type | Description | Use Cases | Privacy Exposure |
|------|-------------|-----------|------------------|
| **VP** | General VP verification method | ID verification, education verification, qualification verification | Selective disclosure of necessary information only |
| **ZKP** | Zero-knowledge proof based verification | Age proof, income range proof, residence proof | Minimum information proof only |

#### **Endpoints Management**

Section for managing endpoint URLs to be connected with the service.

| Function | Description | URL Format Example |
|----------|-------------|-------------------|
| **ADD ENDPOINT** | Add new endpoint URL | `https://example.org/verifier/api/v1` |
| **Delete (🗑️)** | Remove registered endpoint | Click trash icon in each row |

#### **Registration Button Functions**

| Button | Function | Action |
|--------|----------|--------|
| **REGISTER** | Register service with entered information | Create service after validation and navigate to list page |
| **RESET** | Initialize all input fields | Clear all entered content and return to default values |
| **CANCEL** | Cancel registration and navigate to previous page | Return to service list without saving input content |

---

## 3.3. VP Policy Management

The current menu path is `OpenDID > VP`.

VP Policy Management is a function that configures and manages policies for general VP (Verifiable Presentation) verification. VP policies are configured step by step in the order **Filter → Process → Profile → Policy**, with each step's settings forming the foundation for the next step.

VP verification is a method where users selectively disclose necessary information from their owned VCs (Verifiable Credentials) to prove their identity, mainly used for ID verification, education verification, qualification verification, etc.

### 3.3.1. Filter Management

Filter is a core component that determines "what VCs to require from users?", defining VC schema ID, required claims, allowed issuers, etc. Since all VP Profiles must be connected to at least one Filter, it must be set up as the first step in VP policy configuration.

#### **3.3.1.1 Filter Management Screen Layout**

<img src="./images/filter_management.jpg" width="800"/>

| Number | Component | Function Description | When to Use |
|--------|-----------|---------------------|-------------|
| 1 | **REGISTER Button** | Navigate to page for registering new Filter | When creating new Filter |
| 2 | **UPDATE Button** | Modify selected Filter information | When changing existing Filter |
| 3 | **DELETE Button** | Delete selected Filter | When removing unnecessary Filter |
| 4 | **Filter List** | Display summary information of all registered Filters | Understanding Filter status |
| 5 | **Search and Paging** | Filter search and page-by-page navigation | Managing large amounts of data |

#### **Filter List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Title** | Unique title of the Filter | Navigates to detail page when clicked |
| **Type** | Schema type | `OsdSchemaCredential` |
| **Created At** | Registration date/time | Date and time when first registered |

> **Important**: Filters used by a Profile cannot be deleted. To delete, you must first remove the corresponding Filter from all connected Profiles.

---

#### **3.3.1.2. Filter Registration / Modification**

Click the **REGISTER** button to register a new Filter and navigate to the Filter registration page.
The same screen layout is used when **UPDATE** modifying Filters.

<img src="./images/filter_registration.png" width="800"/>

##### **Basic Information Input**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Title*** | Unique title of the Filter | `NationalIdFilter`, `DriverLicenseFilter` | ✅ Required |
| **ID*** | VC schema ID used for verification. It is filled in automatically when a schema is selected with the **SEARCH** button. | `https://example.org/issuer/api/v1/vc/vcschema?name=national_id` | ✅ Required |
| **Type** | Schema type selection | `OsdSchemaCredential` | ✅ Required (fixed value) |
| **Present All** | Whether all claims submission is required | `true` | Default `true` |

**Present All Setting Guide:**
- **true**: All claims in the schema must be submitted (default)
- **false**: Only claims specified in Required Claims need to be submitted

##### **VC Schema Search and Selection**

Since Filter operates based on specific VC schemas, you must first select the schema to use for verification.

<img src="./images/schema-search-section.png" width="600"/>

| Function | Description | Usage |
|----------|-------------|-------|
| **SEARCH Button** | Query schema list from List operator and select | Displays schema selection dialog when clicked |

**Information available in schema selection dialog:**
- **Schema Title**: Human-readable schema name
- **Schema ID**: Unique schema identifier provided by List operator

##### **Required Claims Management**

Define claims that must be submitted during verification.

| Function | Description | Operation Method |
|----------|-------------|------------------|
| **Claims List** | Required claims filled in automatically when a schema is selected | Displayed as a code combining the namespace and claim ID (e.g., `org.opendid.national_id.name`) |

**Claim Selection Process:**
1. Click **SEARCH** button
2. Display **Schema Selection Dialog**
3. **Select a schema** and click **SELECT**
4. **Claims based on selected schema are automatically added**

##### **Display Claims Management**

Set claim information to be displayed to users. Display Claims are managed separately from Required Claims and serve to clearly inform users what information is required during the verification process. (For implementation purposes, values are identical to Required Claims.)

##### **Allowed Issuers Management**

You can restrict the issuers of VCs submitted through this Filter.

| Function | Description | Input Format |
|----------|-------------|--------------|
| **ADD Button** | Add new allowed issuer DID | DID format: `did:example:issuer` |
| **Issuer DID List** | List of currently allowed issuers' DIDs | Display verification authority for each DID |
| **Delete (🗑️)** | Remove specific issuer from allowed list | Click trash icon in corresponding DID row |

##### **Registration Completion**

Once all information input is complete, use the following buttons to finalize registration.

| Button | Function | Action Result |
|--------|----------|---------------|
| **REGISTER** | Register Filter with entered information | Create Filter after validation and navigate to list page |
| **UPDATE** | Modify selected Filter information | When changing existing Filter |
| **RESET** | Initialize all input fields | Clear all entered content and return to default values |
| **CANCEL** | Cancel registration and navigate to previous page | Return to Filter list without saving input content |

---

### 3.3.2. Process Management

Process Management is a function that defines **encryption methods and authentication procedures to be used during VP submission process**.

Process is a core component that determines "how to secure the VP submission process?", defining user authentication methods (PIN/biometric authentication), data encryption settings, and endpoints to process results after verification completion. If Filter defines "what" to verify, Process defines "how" to verify securely.

#### **3.3.2.1. Process Management Screen Layout**

<img src="./images/process_management.png" width="800"/>

| Number | Component | Function Description | When to Use |
|--------|-----------|---------------------|-------------|
| 1 | **REGISTER Button** | Navigate to page for registering new Process | When creating new Process |
| 2 | **UPDATE Button** | Modify selected Process information | When changing existing Process |
| 3 | **DELETE Button** | Delete selected Process | When removing unnecessary Process |
| 4 | **Process List** | Display summary information of all registered Processes | Understanding Process status |
| 5 | **Search and Paging** | Process search and page-by-page navigation | Managing large amounts of data |

#### **Process List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Title** | Unique title of the Process | Name identifiable by administrators |
| **Auth Type** | User authentication method | `PIN or BIO` (PIN or biometric authentication). This is the only value that can be selected on the registration screen. |
| **Curve** | Elliptic curve cryptography algorithm | `Secp256r1` and other cryptographic standards |
| **Cipher** | Symmetric key encryption algorithm | `AES-128-CBC`, `AES-256-CBC`, etc. |
| **Padding** | Padding method | `PKCS5` |

> **Important**: Processes used by a Profile cannot be deleted. To delete, you must first remove the corresponding Process from all connected Profiles.

---

#### **3.3.2.2. Process Registration / Modification**

Click the **REGISTER** button to register a new Process and navigate to the Process registration page.</br>
The same screen layout is used when **UPDATE** modifying Processes.

<img src="./images/process_registration.png" width="800"/>

##### **Basic Information Input**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Title*** | Unique title of the Process | `NationalIdProcess` | ✅ Required |
| **Auth Type*** | User authentication method selection | `PIN or BIO` | ✅ Required (fixed value) |

##### **ReqE2e Information (End-to-End Encryption Settings)**

Set encryption algorithms and methods to be used during VP submission process.

| Encryption Component | Description | Supported Options | Required |
|---------------------|-------------|-------------------|----------|
| **Curve*** | Elliptic curve cryptography algorithm | `Secp256r1` | ✅ Required `Secp256r1` (fixed value) |
| **Cipher*** | Symmetric key encryption algorithm | `AES-256-CBC` | ✅ Required `AES-256-CBC` (fixed value) |
| **Padding*** | Block encryption padding method | `PKCS5` | ✅ Required `PKCS5` (fixed value) |

##### **Endpoints Management**

Set external endpoints to handle events occurring during Process execution.

| Function | Description | URL Format Example |
|----------|-------------|-------------------|
| **ADD Button** | Add new endpoint URL | `https://example.org/verifier/api/v1/request-verify` |
| **Endpoint List** | Display all currently registered URLs | Can check purpose and status of each URL |
| **Delete Column** | Remove specific endpoint | Click Delete button in corresponding row |

##### **Registration Completion**

Once all information input is complete, use the following buttons to finalize registration.

| Button | Function | Action Result |
|--------|----------|---------------|
| **REGISTER** | Register Process with entered information | Create Process after encryption settings validation and navigate to list page |
| **UPDATE** | Modify selected Process information | When changing existing Process |
| **RESET** | Initialize all input fields | Clear all entered content and return to default values |
| **CANCEL** | Cancel registration and navigate to previous page | Return to Process list without saving input content |

---

### 3.3.3. Profile Management

Profile Management is a function that **combines previously defined Filter and Process to create complete VP verification profiles**.

Profile is an integrated component that defines "what VC to verify in what way?", combining Filter (verification target) and Process (verification method) to complete the verification experience to be provided to users. Since Profile is the direct foundation for final Policy creation, it is a core step in VP policy configuration.

#### **3.3.3.1. Profile Management Screen Layout**

<img src="./images/profile_management.jpg" width="800"/>

| Number | Component | Function Description | When to Use |
|--------|-----------|---------------------|-------------|
| 1 | **REGISTER Button** | Navigate to page for registering new Profile | When creating new Profile |
| 2 | **UPDATE Button** | Modify selected Profile information | When changing existing Profile |
| 3 | **DELETE Button** | Delete selected Profile | When removing unnecessary Profile |
| 4 | **Profile List** | Display summary information of all registered Profiles | Understanding Profile status |
| 5 | **Search and Paging** | Profile search and page-by-page navigation | Managing large amounts of data |

#### **Profile List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Title** | Unique title of the Profile | Navigates to detail page when clicked |
| **Description** | Profile description | Description of purpose and characteristics |
| **Created At** | Registration date/time | Date and time when first registered |

> **Warning**: Profiles already registered in Policy Management cannot be deleted. To delete, you must first remove the corresponding profile from connected Policies.

---

#### **3.3.3.2. Profile Registration / Modification**

Click the **REGISTER** button to register a new Profile and navigate to the Profile registration page.
The same screen layout is used when **UPDATE** modifying Profiles.

<img src="./images/profile_register.jpg" width="800"/>

##### **Basic Information Input**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Title*** | Unique title of the Profile | `NationalIdProfile` | ✅ Required |
| **Type** | Profile type | `VerifyProfile` | ✅ Required (fixed value) |
| **Description** | Detailed description of the Profile | `VP profile for national ID verification` | Optional |
| **Encoding*** | Character encoding method | `UTF-8` | ✅ Required |
| **Language*** | Supported language | `ko` (Korean), `en` (English) | ✅ Required |

##### **Verifier Information (Automatic Setup)**

Verifier information is automatically entered through the membership certificate and cannot be modified.

| Information Item | Auto-set Content | Source |
|-----------------|------------------|--------|
| **DID** | `did:example:verifier` | DID generated during Verifier registration |
| **Name** | Registered Verifier name | Name set in Verifier Management |
| **Cert VC Ref** | Membership certificate access URL | Certificate VC location issued by TA |
| **Ref** | Verifier server URL | URL in Verifier Management |

> **Note**: This information is important trust information that tells users "who is requesting verification?" during VP verification.

##### **Process Information Selection**

Select the Process to connect to the Profile.

<img src="./images/process-selection-section.png" width="300"/>

| Function | Description | Selection Criteria |
|----------|-------------|-------------------|
| **Process Selection Field** | Display name of currently selected Process | Read-only field |
| **Search Button** | Open Process search dialog | Select from all registered Processes |

##### **Filter Information Selection**

Select the Filter to connect to the Profile.

<img src="./images/filter-selection-section.png" width="300"/>

| Function | Description | Selection Criteria |
|----------|-------------|-------------------|
| **Filter Selection Field** | Display name of currently selected Filter | Read-only field |
| **Search Button** | Open Filter search dialog | Select from all registered Filters |

##### **Registration Completion**

Once all information input is complete, use the following buttons to finalize registration.

| Button | Function | Action Result |
|--------|----------|---------------|
| **REGISTER** | Register Profile with entered information | Create Profile after Filter/Process combination validation and navigate to list page |
| **RESET** | Initialize all input fields | Clear all entered content and return to default values (including selected Filter/Process) |
| **CANCEL** | Cancel registration and navigate to previous page | Return to Profile list without saving input content |

### 3.3.4. Policy Management

Policy Management is a function that **connects previously configured Service and Profile to create complete VP verification policies**.

Policy is the final step in VP policy configuration, defining "what service to verify in what way?" as executable verification rules. It combines Service (verification purpose and result processing) and Profile (verification target and method) to complete the entire verification workflow that actual users will experience. Generated Policies are core components directly referenced when external systems request VP verification.

#### **3.3.4.1. Policy Management Screen Layout**

<img src="./images/policy_management.jpg" width="800"/>

| Number | Component | Function Description | When to Use |
|--------|-----------|---------------------|-------------|
| 1 | **REGISTER Button** | Navigate to page for registering new Policy | When creating new Policy |
| 2 | **UPDATE Button** | Modify selected Policy information | When changing existing Policy |
| 3 | **DELETE Button** | Delete selected Policy | When removing unnecessary Policy |
| 4 | **Policy List** | Display summary information of all registered Policies | Understanding Policy status |
| 5 | **Search and Paging** | Policy search and page-by-page navigation | Managing large amounts of data |

#### **Policy List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Policy Title** | Unique title of the Policy | Navigates to detail page when clicked |
| **Payload Service** | Connected Service name | Service used by this Policy |
| **Profile Title** | Connected Profile name | Profile used by this Policy |
| **Created At** | Registration date/time | Date and time when first registered |

---

#### **3.3.4.2. Policy Registration**

Click the **REGISTER** button to register a new Policy and navigate to the Policy registration page.

<img src="./images/policy_registration.png" width="800"/>

##### **Basic Information Input**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Policy Title*** | Unique title of the Policy | `NationalIdPolicy`, `IDCardVerificationPolicy` | ✅ Required |

##### **Profile Information Selection**

Select the Profile to connect to the Policy.

<img src="./images/profile-selection-section.png" width="400"/>

| Function | Description | Selection Criteria |
|----------|-------------|-------------------|
| **Profile Selection Field** | Display name of currently selected Profile | Read-only field |
| **SEARCH Button** | Open Profile search dialog | Select from all registered Profiles |

**Information available in Profile selection dialog:**
- **Profile Title**: Name and purpose of the profile

##### **Payload Information Selection**

Select the Service to connect to the Policy.

<img src="./images/payload-selection-section.png" width="400"/>

| Function | Description | Selection Criteria |
|----------|-------------|-------------------|
| **Payload Selection Field** | Display name of currently selected Service | Read-only field |
| **SEARCH Button** | Open Service search dialog | Select from all registered Services |

**Information available in Service (Payload) selection dialog:**
- **Title**: Name and purpose of the service

##### **Registration Completion**

Once all information input is complete, use the following buttons to finalize registration.

| Button | Function | Action Result |
|--------|----------|---------------|
| **REGISTER** | Register Policy with entered information | Create Policy after Service/Profile compatibility validation and navigate to list page |
| **RESET** | Initialize all input fields | Clear all entered content and return to default values |
| **CANCEL** | Cancel registration and navigate to previous page | Return to Policy list without saving input content |

---
<br/>

## 3.4. ZKP Policy Management

The current menu path is `OpenDID > ZKP`.

ZKP Policy Management is a menu for managing ZKP (Zero-Knowledge Proof) policies. It consists of multiple sub-menus, each managing specific aspects of ZKP policy.

### 3.4.1. Proof Request Configuration

The Proof Request Configuration menu allows you to register, modify, and delete proof request information used for ZKP verification.

<img src="./images/proof_request_configuration.jpg" width="800"/>

Proof Request Configuration screen provides the following main functions:
- REGISTER: Register new proof request.
- UPDATE: Modify selected proof request (activated when item is selected).
- DELETE: Delete selected proof request (activated when item is selected).

The proof request list table displays information such as Name, Version, Profile Count, Registered At, Updated At, and you can navigate multiple pages of data through pagination functionality.

#### 3.4.1.1 Proof Request Register

The proof request registration page allows you to input information for registering new proof requests. This page also uses the same form when modifying proof requests.

<img src="./images/proof_request_registration.jpg" width="800"/>

| Number | Item | Description |
|--------|------|-------------|
| 1 | Name | Field for entering the name of the proof request. "zkpProofRequest" is entered as an example. |
| 2 | Version | Enter the version of the proof request. "1.0" is entered as an example. |
| 3 | Curve | Select the elliptic curve algorithm to use. You can select options like "SECP-256-R1" from the dropdown menu. |
| 4 | Cipher | Select the encryption algorithm. You can select options like "AES-256-CBC" from the dropdown menu. |
| 5 | Padding | Select the padding method. You can select options like "PKCS5" from the dropdown menu. |
| 6 | Requested Attributes | Section for managing attributes to request. Click the "ADD ATTRIBUTE" button to add attributes by selecting schemas from a popup. Each attribute consists of Attribute Name and Credential Definition. To delete an entered attribute, click the trash icon on the right of the corresponding item. |
| 7 | Requested Predicates | Section for managing predicates to request. Click the "ADD PREDICATE" button to add attributes by selecting schemas from a popup. Each predicate consists of Attribute Name, Predicate Type, Predicate Value, and Credential Definition. To delete an entered predicate, click the trash icon on the right of the corresponding item. |
| 8 | Button Area | - REGISTER: Register proof request with entered information.<br>- RESET: Initialize all input fields. In modification mode, revert to existing input values.<br>- CANCEL: Cancel input and return to previous page (proof request list). |

 > **Note**: For detailed information about data used in ZKP verification, please refer to the Data Specification document.

### 3.4.2. ZKP Profile Management

The ZKP Profile Management menu allows you to manage ZKP profile information. In this screen, you can check the profile list and perform registration, modification, and deletion operations.

<img src="./images/zkp_profile_management.jpg" width="800"/>

ZKP Profile Management screen provides the following main functions:
- REGISTER: Register new ZKP profile.
- UPDATE: Modify selected ZKP profile (activated when item is selected).
- DELETE: Delete selected ZKP profile (activated when item is selected).

The ZKP profile list table displays information such as Profile ID, Profile Title, Profile Description, Used in Policies, Registered At, Updated At, and you can navigate multiple pages of data through pagination functionality.

> **Warning**: Profiles already registered in ZKP Policy Management cannot be deleted. To delete, you must first remove the corresponding profile from connected Policies.

#### 3.4.2.1 ZKP Profile Register

The ZKP profile registration page allows you to input information for registering new ZKP profiles. This page also uses the same form when modifying profiles.

<img src="./images/zkp_profile_registration.jpg" width="800"/>

| Number | Item | Description |
|--------|------|-------------|
| 1 | Title | Field for entering the title of the ZKP profile. "ZKPTestProfile" is entered as an example. |
| 2 | Description | Field for entering description of the ZKP profile. "Test for ZKP Proof Verification" is entered as an example. |
| 3 | Encoding | Select the encoding method used in the profile. You can select options like "UTF-8" from the dropdown menu. |
| 4 | Language | Select the profile language. You can select options like "English" from the dropdown menu. |
| 5 | Proof Request | Select the Proof Request to connect to the profile. You can select registered proof requests like "zkpProofRequest" from the dropdown menu. |
| 6 | Button Area | - REGISTER: Register ZKP profile with entered information.<br>- RESET: Initialize all input fields. In modification mode, revert to existing input values.<br>- CANCEL: Cancel input and return to previous page (ZKP profile list). |

### 3.4.3. ZKP Policy Management

The ZKP Policy Management menu allows you to comprehensively manage entire ZKP policies. In this screen, you can check the policy list and perform registration, modification, and deletion operations.

<img src="./images/zkp_policy_management.jpg" width="800"/>

ZKP Policy Management screen provides the following main functions:
- REGISTER: Register new ZKP policy.
- UPDATE: Modify selected ZKP policy (activated when item is selected).
- DELETE: Delete selected ZKP policy (activated when item is selected).

The ZKP policy list table displays information such as Policy Title, Payload Service, Profile Title, Created At, and you can navigate multiple pages of data through pagination functionality.

ZKP policy is an important element for ZKP verification, defining the entire ZKP verification process by connecting ZKP Profile and Service.

#### 3.4.3.1 ZKP Policy Register

The ZKP policy registration page allows you to input information for registering new ZKP policies. This page also uses the same form when modifying policies.

<img src="./images/zkp_policy_registration.jpg" width="800"/>

ZKP policy registration screen consists of the following items:

| Number | Item | Description |
|--------|------|-------------|
| 1 | Policy Title | Field for entering the title of the ZKP policy. "ZKPPolicy" is entered as an example. |
| 2 | Profile Information | Select ZKP profile information to connect to the policy. Click the "SEARCH" button to display the ZKP profile search popup, and the selected profile after search is automatically entered in this field. |
| 3 | Payload Information | Select service information to use in the policy. Click the "SEARCH" button to display the service search popup, and the selected service after search is automatically entered in this field. |
| 4 | Button Area | - REGISTER: Register ZKP policy with entered information.<br>- RESET: Initialize all input fields. In modification mode, revert to existing input values.<br>- CANCEL: Cancel input and return to previous page (ZKP policy list). |

Main components required for ZKP policy registration are as follows:
- Policy Title: Unique name of the ZKP policy
- Profile: Profile information for ZKP verification
- Payload Service: Service information to be used when requesting ZKP

<br/>

### 3.5. VP History

The VP History menu allows you to view the history of VP (Verifiable Presentation) submitted by users. In this screen, you can check the status and detailed information of each VP submission attempt, and track not only successful submissions but also failed or ongoing requests.

<img src="./images/vp_history.jpg" width="800"/>

VP History provides the following main functions and information:

| Number | Item | Description |
|--------|------|-------------|
| 1 | VP Submit List | Title of VP submission list. |
| 2 | Transaction Status Filter | Filter only VP submissions of specific status (All, Completed, Pending, Failed) through dropdown menu. |
| 3 | List Header | Headers for VP submission information such as Transaction Status, Protocol, Transaction ID, Holder DID, Error Code, and Format. Protocol shows the submission method (`OpenDID` or `OID4VP`). |
| 4 | Status Display | Distinguish VP submission status by color. Completed cases are displayed as green background 'Completed', pending cases as yellow background 'Pending', failed cases as red background 'Failed'. |
| 5 | Transaction ID | Unique transaction identifier for each VP submission attempt is displayed. All status requests can be tracked through this. |
| 6 | DID Information | Holder DID (DID of user who submitted VP) and submission time (Created At) for each VP submission case are displayed. For ZKP, this value is entered as an arbitrary value of "ZKP holder DID". For Pending or Failed status, it is displayed as "-". |
| 7 | Pagination | Select the number of rows to display per page and navigate between pages. Actual totals vary by environment. |

Through VP history, administrators can perform the following tasks:
- Understanding VP submission status of specific status (Completed, Pending, Failed)
- Viewing VP submission history of specific user (Holder DID)
- Problem tracking and debugging through Transaction ID of failed requests

## 3.6. Admin Management

The `Admin Management` menu is a function for managing administrator accounts that can access the Verifier Admin Console.

When you install the Verifier server, the `admin@opendid.omnione.net` account is automatically created with ROOT privileges by default.
This account is the only ROOT account in the system and cannot be deleted.

Administrator accounts are divided into two privilege types: **ROOT** and **Normal Admin**.
ROOT accounts can perform all functions in the `Admin Management` menu, while Normal Admin can only perform general inquiry functions.

---
> **Note:** Currently, the privilege difference between ROOT accounts and Normal Admin accounts is
> only the difference in buttons displayed in the `Admin Management` menu (only Root can REGISTER / DELETE / CHANGE PASSWORD).
> Access privileges or functional restrictions for other menus in the system are not yet applied.
---

<br/>

## 3.6.1 Admin List View

When you enter the `Admin Management` menu, the list of registered administrator accounts is displayed in table format.

<img src="./images/admin-management.png" width="800"/>

| Number | Item                    | Description                                                             |
| ------ | ----------------------- | ----------------------------------------------------------------------- |
| 1      | **REGISTER Button**     | Navigate to registration page where you can register new administrator account. |
| 2      | **DELETE Button**       | Delete selected administrator account. (Only ROOT administrator can do this) |
| 3      | **CHANGE PASSWORD Button** | Change password of selected administrator account.                    |
| 4      | **ID**                  | Email ID of registered administrator account.                          |
| 5      | **Role**                | Role of the administrator account (`ROOT`, `NORMAL`)     |
| 6      | **Registered At**       | Date and time when the account was first registered.                   |
| 7      | **Updated At**          | Date and time when last modified.                                      |

<br/>

## 3.6.2. Admin Registration

Click the **REGISTER** button in the `Admin Management` screen to navigate to the registration screen as shown below.

<img src="./images/admin-registration.png" width="800"/>

| Number | Item                        | Description                                                                |
| ------ | --------------------------- | -------------------------------------------------------------------------- |
| 1      | **ID**                      | ID of administrator account to register. Must use email format.           |
| 2      | **Check Availability Button** | Check if entered ID is not duplicated.                                   |
| 3      | **Role**                    | Select privileges of administrator account to register (e.g., Normal Admin) |
| 4      | **Password**                | Enter password to use for login.                                          |
| 5      | **Re-enter Password**       | Enter password again to check for match.                                  |
| 6      | **REGISTER Button**         | Register administrator account based on entered information.               |
| 7      | **RESET Button**            | Initialize all input values.                                              |
| 8      | **CANCEL Button**           | Cancel registration and return to previous screen.                        |

## 3.7. OID4VP Management

The `OID4VP` menu manages the Verifier settings and verification policies required for OpenID4VP (OID4VP) VP submission. Configure them in the following order.

| Order | Menu | What It Defines |
|-------|------|-----------------|
| 1 | **Configuration** | Verifier information passed to the Wallet, session, encryption, and verification options |
| 2 | **DCQL Scope Management** | Credentials and claims requested from the Wallet (DCQL Query) |
| 3 | **Policy Management** | The link between a verification request Policy and a DCQL Scope |

### 3.7.1. Configuration

`OID4VP > Configuration` sets the Verifier information used with Wallets. The server has a single configuration, and it applies to all OID4VP requests.

<img src="./images/oid4vp-config.png" width="800"/>

##### **Basic Information**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Base URL*** | Verifier server address. It is the base of the request and response URLs passed to the Wallet. | `https://example.org` | ✅ Required |
| **Client Name*** | Name displayed to Wallet users. Click the **VERIFIER** button to select the registered Verifier name. | `Example Verifier` | ✅ Required |

##### **Client ID**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Client ID Scheme** | How the Verifier is identified. Select `decentralized_identifier` or `redirect_uri`. | `decentralized_identifier` | Default `decentralized_identifier` |
| **Client ID Value*** | For `decentralized_identifier`, enter the Verifier DID. Click the **DID SEARCH** button to select the registered DID. For `redirect_uri`, `Base URL` + `/oid4vp/response` is filled in automatically and can be edited for proxy environments. | `did:example:verifier` | ✅ Required |
| **client_id** | Preview of the final value combined from the two values above. | `decentralized_identifier:did:example:verifier` | Displayed automatically |

##### **Session**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Session TTL (ms)** | OID4VP session lifetime. Enter a value from 300,000 (5 minutes) to 600,000 (10 minutes). | `300000` | Default `300000` |
| **Invocation Scheme** | Scheme of the URI that invokes the Wallet. | `openid4vp://` | Default `openid4vp://` |

##### **Encryption**

This area is read only.

| Item | Displayed Value | Description |
|------|-----------------|-------------|
| **Response Mode** | `direct_post.jwt` | Fixed value. All transactions use an encrypted response, and plaintext submissions are rejected. |
| **VP Token Encryption Key** | Partially masked | Symmetric key for VP Token encryption. It cannot be changed on this screen and is managed through server configuration. |
| **JWE Key Agreement (alg)** | `ECDH-ES` | Key agreement algorithm for encrypted responses. Changing it requires prior agreement with Wallets. |
| **JWE Content Encryption (enc)** | `A256GCM` | Content encryption algorithm for encrypted responses. Changing it requires prior agreement with Wallets. |

##### **Verification**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Enforce Claim Constraints** | When on, the claim constraints of the DCQL Query are enforced during VP verification. | Off | Default off |

##### **Saving**

| Button | Function | Operation Result |
|--------|----------|------------------|
| **SAVE** | Save changed settings | Enabled only when there are changes. An error is displayed if Base URL, Client Name, or Client ID Value is empty. |
| **RESET** | Revert input | Reverts to the last saved values. |

**JSON Preview** at the bottom of the screen is a read-only area that shows the entire configuration to be saved as JSON. The VP Token Encryption Key is partially masked here as well.

> **Note**: Before saving, check that the values match those used with Wallets.

---

### 3.7.2. DCQL Scope Management

`OID4VP > DCQL Scope Management` links a Scope name to a DCQL Query. The DCQL Query defines which credentials and claims are requested from the Wallet. When OID4VP verification starts, the DCQL Query of the Scope linked to the Policy is used in the request.

#### **3.7.2.1. DCQL Scope Management Screen Layout**

<img src="./images/oid4vp-scope.png" width="800"/>

| Number | Component | Function Description | Usage Timing |
|--------|-----------|----------------------|--------------|
| 1 | **REGISTER Button** | Navigate to page for registering new Scope | When creating new Scope |
| 2 | **UPDATE Button** | Modify selected Scope information | When changing existing Scope |
| 3 | **DELETE Button** | Delete selected Scope | When removing unnecessary Scope |
| 4 | **Scope List** | Display summary information of all registered Scopes | Understanding Scope status |
| 5 | **Refresh and Paging** | Reload the list and navigate page by page | Managing large amounts of data |

#### **Scope List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Scope** | Unique name of the Scope | Navigates to detail page when clicked |
| **Description** | Scope description | Description of purpose and characteristics |
| **Enabled** | Whether it is in use | `Yes` / `No` |
| **Created At** | Registration date/time | Date and time when first registered |

> **Caution**: A Scope linked to a Policy can also be deleted. Before deleting, check whether any Policy uses the Scope.

---

#### **3.7.2.2. Scope Registration / Modification**

Click the **REGISTER** button to register a new Scope and navigate to the Scope registration page. The same screen layout is used when **UPDATE** modifying Scopes, where the save button is displayed as **SAVE**. The screen below is an example of the `dc+sd-jwt-did` format.

<img src="./images/oid4vp-scope-registration.png" width="800"/>

##### **Basic Information**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Scope*** | Unique name of the Scope. A name that is already registered cannot be used. | `VerifiableIdSD` | ✅ Required |
| **Description** | Scope description | `Verifiable ID with selective disclosure` | Optional |
| **Enabled** | Whether it is in use. When off, the Scope is not used for OID4VP requests and does not appear in the Scope search of Policy registration. | On | Default on |

##### **CredentialQuery**

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Credential ID*** | ID that identifies the credential within the DCQL Query. Only letters, numbers, `_`, and `-` are allowed. | `pid` | ✅ Required |
| **Format** | Credential format to request. Changing the format clears the Meta value and Claims. | `dc+sd-jwt-did` | Default `dc+sd-jwt-did` |

##### **Input by Format**

How Meta and Claims are entered depends on the Format.

| Format | Meta Key | Meta Value Input | Claims Input |
|--------|----------|------------------|--------------|
| `dc+sd-jwt-did`, `vc+sd-jwt` | `vct_values` | Enter as a JSON array (e.g., `["urn:eudi:pid:1"]`) | Enter directly when using **Selective claims** |
| `opendid_vc` | `credential_schema_id_values` | Filled in automatically when a schema is selected with **SCHEMA SEARCH** | Claims of the selected schema are filled in automatically and can only be removed |
| `mso_mdoc`, `mso_mdoc-did` | `doctype_value` | Enter as a docType string (e.g., `org.iso.18013.5.1.mDL`) | Enter Namespace and Claim Name directly (optional) |

##### **Disclosure Mode**

This is not displayed for the `mso_mdoc` and `mso_mdoc-did` formats.

| Mode | Description |
|------|-------------|
| **Full credential** | No claims are specified, so the Wallet discloses the full credential. |
| **Selective claims** | Only the claims specified in Claims below are requested from the Wallet. |

##### **Claims**

Add a row with the **ADD CLAIM** button and remove it with the trash icon.

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **ID** | Claim identifier | `claim_id` | Optional |
| **Path (JSON Array)** | Claim path. Rows left empty are not included in the DCQL Query. | `["family_name"]` | Enter for each row |
| **Values (Optional)** | List of allowed values | `["val1","val2"]` | Optional |

**`mso_mdoc` format example**

Specify a claim with ID (optional), Namespace, and Claim Name.

<img src="./images/oid4vp-scope-registration-mdoc.png" width="800"/>

**`opendid_vc` format example**

Select a VC schema registered in the TA with **SCHEMA SEARCH** to fill in the Meta value and Claims. Remove claims you do not need with the trash icon.

<img src="./images/oid4vp-scope-registration-opendid.png" width="800"/>

##### **DCQL Query JSON Preview**

A read-only area that shows the DCQL Query built from the entered values in real time.

##### **Registration Completion**

| Button | Function | Operation Result |
|--------|----------|------------------|
| **REGISTER** | Register Scope with entered information | After validation, creates the Scope and navigates to the list page |
| **RESET** | Clear input | Clears the entered content. The selected Format is kept. |
| **CANCEL** | Cancel registration and move to previous page | Returns to the Scope list without saving |

---

#### **3.7.2.3. Scope Details**

Click a Scope name in the list to navigate to the detail page.

<img src="./images/oid4vp-scope-detail.png" width="800"/>

| Item | Description |
|------|-------------|
| **Scope** | Unique name of the Scope |
| **Description** | Scope description |
| **Enabled** | Whether it is in use (`Yes` / `No`) |
| **DCQL Query (JSON)** | Saved DCQL Query |
| **BACK Button** | Returns to the Scope list |
| **GO TO EDIT Button** | Navigates to the Scope modification page |

---

### 3.7.3. Policy Management

`OID4VP > Policy Management` registers and manages the Policies used for OID4VP verification. An OID4VP Policy is linked to one DCQL Scope, and this Policy is specified when OID4VP verification starts.

#### **3.7.3.1. Policy Management Screen Layout**

<img src="./images/oid4vp-policy.png" width="800"/>

| Number | Component | Function Description | Usage Timing |
|--------|-----------|----------------------|--------------|
| 1 | **REGISTER Button** | Navigate to page for registering new Policy | When creating new Policy |
| 2 | **UPDATE Button** | Modify selected Policy information | When changing existing Policy |
| 3 | **DELETE Button** | Delete selected Policy | When removing unnecessary Policy |
| 4 | **Policy List** | Display summary information of all registered Policies | Understanding Policy status |
| 5 | **Search and Paging** | Search by Title and navigate page by page | Managing large amounts of data |

#### **Policy List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Policy Title** | Unique title of the Policy | Navigates to detail page when clicked |
| **Scope** | Linked DCQL Scope | Scope used by this Policy |
| **Created At** | Registration date/time | Date and time when first registered |

---

#### **3.7.3.2. Policy Registration / Modification**

Click the **REGISTER** button to register a new Policy and navigate to the Policy registration page. The same screen layout is used when **UPDATE** modifying Policies, where the save button is displayed as **SAVE**.

<img src="./images/oid4vp-policy-registration.png" width="800"/>

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Policy Title*** | Unique title of the Policy | `VerifiableIdSD` | ✅ Required |
| **Scope*** | DCQL Scope to link. Select it with the **SEARCH** button. | `VerifiableIdSD` | ✅ Required |

Click the **SEARCH** button to open the Scope search dialog. Only Scopes with `Enabled` turned on are displayed.

<img src="./images/oid4vp-policy-scope-search.png" width="400"/>

##### **Registration Completion**

| Button | Function | Operation Result |
|--------|----------|------------------|
| **REGISTER** | Register Policy with entered information | After validation, creates the Policy and navigates to the list page |
| **RESET** | Clear input | Clears the entered content. |
| **CANCEL** | Cancel registration and move to previous page | Returns to the Policy list without saving |

---

#### **3.7.3.3. Policy Details**

Click a Policy title in the list to navigate to the detail page.

<img src="./images/oid4vp-policy-detail.png" width="800"/>

| Item | Description |
|------|-------------|
| **Policy ID** | ID assigned automatically at registration |
| **Policy Title** | Unique title of the Policy |
| **Scope** | Linked DCQL Scope |
| **BACK Button** | Returns to the Policy list |
| **GO TO EDIT Button** | Navigates to the Policy modification page |

---

## 3.8. Log Management

After Verifier registration, `Log Management` shows API calls in newest first order with search, pagination, and refresh. Select `Called At` to open a detail dialog. Logs are read only.

### 3.8.1. API Log

`Log Management > API Log` shows Verifier Agent and protocol API calls. The path is `/logs/api`.

<img src="./images/api-log-api-example.png" width="800"/>

#### **API Log List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Called At** | Call date/time | Opens the detail dialog when clicked |
| **Method** | HTTP method | `GET`, `POST`, etc. |
| **URI** | Called API path | `/verifier/api/v1/request-verify`, etc. |
| **Status** | HTTP status code | `200`, `400`, etc. |
| **Result** | Processing result | `SUCCESS` / `FAIL` |
| **Requester ID** | Requester ID | `anonymousUser` for unauthenticated calls |

Search by `URI` or `Result`.

#### **API Log Details**

<img src="./images/api-log-api-detail-example.png" width="800"/>

In addition to the list columns, the following items are available. Each item is displayed when it was recorded.

| Item | Description |
|------|-------------|
| **Duration Ms** | Processing time |
| **Client IP** | IP of the calling client |
| **User Agent** | Information about the calling client |
| **Message** | Error message |

### 3.8.2. Audit API Log

`Log Management > Audit API Log` shows administrator actions through Admin APIs. The path is `/logs/audit`.

<img src="./images/api-log-audit-example.png" width="800"/>

#### **Audit API Log List Information**

| Column | Display Content | Description |
|--------|----------------|-------------|
| **Called At** | Call date/time | Opens the detail dialog when clicked |
| **Requester ID** | ID of the administrator | `admin@example.org`, etc. |
| **Action Type** | Action type | `NONE`, `CREATE`, `UPDATE`, `DELETE`, `LOGIN`, `LOGOUT` |
| **Target Type** | Target type of the action | `SERVER_CONFIG`, `POLICY`, `SESSION`, etc. |
| **Target ID** | Target ID of the action | `API_KEY_LSS`, etc. |
| **Result** | Processing result | `SUCCESS` / `FAIL` |

Search by `Action Type`, `Target Type`, `Result`, or `Requester ID`.

#### **Audit API Log Details**

<img src="./images/api-log-audit-detail-example.png" width="800"/>

In addition to the list columns, the following items are available. Items other than `Description` are displayed only when they were recorded.

| Item | Description |
|------|-------------|
| **Client IP** | IP of the client that performed the action |
| **Description** | Action description |
| **User Agent** | Information about the client that performed the action |
| **Message** | Error message |

> Records are created for API calls annotated with `@ArticleLog`. The screens use example logs and accounts.

## 3.9. Server Configuration

`Server Configuration` manages the API Keys required to integrate with external servers.

<img src="./images/server-configuration.png" width="800"/>

| Item | Description | Input Example | Required |
|------|-------------|---------------|----------|
| **Ledger Service Server** | API Key issued by the LSS (`API_KEY_LSS`). The Verifier sends it in the `X-API-Key` header when calling the LSS. | `example-key` | ✅ Required |

| Button | Function | Operation Result |
|--------|----------|------------------|
| **UPDATE** | Save the entered value | Enabled only when the value has changed, and saves after a confirmation dialog. |
| **RESET** | Revert input | Reverts to the last saved value. |

> **Note**: If the API Key is not registered, LSS calls fail with `SSRVVRF21501`. A changed value must match the key issued by the LSS.

## 3.10. Password Policy Settings

Use `Admin Management > Password Policy Settings` to view and update password rules for administrator accounts. The rules apply when creating accounts or changing passwords.

<img src="./images/password-policy.png" width="800"/>

##### **Basic Settings**

| Item | Description | Input Example | Constraints |
|------|-------------|---------------|-------------|
| **Minimum Length** | Minimum password length | `8` | 6 to 20 characters |
| **Password Expiration Period** | Password validity period in days | `365` | 30 to 365 days |

##### **Password Composition Rules**

| Item | Description | Input Example |
|------|-------------|---------------|
| **Uppercase Letters (A-Z)** | When on, an uppercase letter is required. | Off |
| **Numbers (0-9)** | When on, a number is required. | Off |
| **Special Characters (!@#$%^&\*)** | When on, a special character is required. | Off |

| Button | Function | Operation Result |
|--------|----------|------------------|
| **UPDATE** | Save the entered policy | Enabled only when values have changed, and saves after a confirmation dialog. |
| **RESET** | Revert input | Reverts to the last saved values. |

[Open DID Installation Guide]: https://github.com/OmniOneID/did-release/blob/main/release-V2.0.0.0/OpenDID_Installation_Guide-V2.0.0.0_ko.md