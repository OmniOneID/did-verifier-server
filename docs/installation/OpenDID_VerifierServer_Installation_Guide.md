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

# Open DID Verifier Server Installation Guide

- Date: 2025-04-01
- Version: v1.0.0

## Table of Contents

- [1. Introduction](#1-introduction)
  - [1.1. Overview](#11-overview)
  - [1.2. Verifier Server Definition](#12-verifier-server-definition)
  - [1.3. System Requirements](#13-system-requirements)
- [2. Prerequisites](#2-prerequisites)
  - [2.1. Git Installation](#21-git-installation)
  - [2.2. PostgreSQL Installation](#22-postgresql-installation)
  - [2.3. Node.js Installation](#23-nodejs-installation)
- [3. Cloning Source Code from GitHub](#3-cloning-source-code-from-github)
  - [3.1. Clone Source Code](#31-clone-source-code)
  - [3.2. Directory Structure](#32-directory-structure)
- [4. Server Deployment Methods](#4-server-deployment-methods)
  - [4.1. Running with IDE (Gradle and React Projects)](#41-running-with-ide-gradle-and-react-projects)
    - [4.1.1. Running Backend (Spring Boot) with IntelliJ IDEA](#411-running-backend-spring-boot-with-intellij-idea)
    - [4.1.2. Running Frontend (React) with VS Code](#412-running-frontend-react-with-vs-code)
  - [4.2. Running with Console Commands](#42-running-with-console-commands)
    - [4.2.1. Gradle Build Commands](#421-gradle-build-commands)
  - [4.3. Running with Docker](#43-running-with-docker)
- [5. Configuration Guide](#5-configuration-guide)
  - [5.1. application.yml](#51-applicationyml)
    - [5.1.1. Spring Basic Configuration](#511-spring-basic-configuration)
    - [5.1.2. Jackson Basic Configuration](#512-jackson-basic-configuration)
    - [5.1.3. Server Configuration](#513-server-configuration)
    - [5.1.4. TAS Configuration](#514-tas-configuration)
  - [5.3. database.yml](#53-databaseyml)
    - [5.3.1. Spring Liquibase Configuration](#531-spring-liquibase-configuration)
    - [5.3.2. Datasource Configuration](#532-datasource-configuration)
    - [5.3.3. JPA Configuration](#533-jpa-configuration)
  - [5.4. application-logging.yml](#54-application-loggingyml)
    - [5.4.1. Logging Configuration](#541-logging-configuration)
  - [5.5. application-spring-docs.yml](#55-application-spring-docsyml)
  - [5.6. application-wallet.yml](#56-application-walletyml)
  - [5.7. application-verifier.yml](#57-application-verifieryml)
  - [5.8. VP policy](#58-vp-policy)
  - [5.9. blockchain.properties](#59-blockchainproperties)
    - [5.9.1. Blockchain Integration Configuration](#591-blockchain-integration-configuration)
- [6. Profile Configuration and Usage](#6-profile-configuration-and-usage)
  - [6.1. Profile Overview (`sample`, `dev`)](#61-profile-overview-sample-dev)
    - [6.1.1. `sample` Profile](#611-sample-profile)
    - [6.1.2. `dev` Profile](#612-dev-profile)
  - [6.2. Profile Configuration Methods](#62-profile-configuration-methods)
    - [6.2.1. When Running the Server Using an IDE](#621-when-running-the-server-using-an-ide)
    - [6.2.2. When Running the Server Using Console Commands](#622-when-running-the-server-using-console-commands)
    - [6.2.3. When Running the Server Using Docker](#623-when-running-the-server-using-docker)
- [7. Building and Running with Docker](#7-building-and-running-with-docker)
  - [7.1. Building Docker Image (`Dockerfile` Based)](#71-building-docker-image-dockerfile-based)
  - [7.2. Running Docker Image](#72-running-docker-image)
  - [7.3. Running with Docker Compose](#73-running-with-docker-compose)
    - [7.3.1. `docker-compose.yml` File Description](#731-docker-composeyml-file-description)
    - [7.3.2. Container Execution and Management](#732-container-execution-and-management)
    - [7.3.3. Server Configuration Method](#733-server-configuration-method)
- [8. Installing Docker PostgreSQL](#8-installing-docker-postgresql)
  - [8.1. Installing PostgreSQL with Docker Compose](#81-installing-postgresql-with-docker-compose)
  - [8.2. Running PostgreSQL Container](#82-running-postgresql-container)

# 1. Introduction

## 1.1. Overview

This document provides a guide for installing, configuring, and running the Open DID Verifier server. The Verifier server consists of a Spring Boot-based backend and a React-based Admin console frontend, which can be deployed together through Gradle build. The document explains the installation process, environment configuration, Docker execution methods, and profile settings step by step, guiding users to efficiently install and run the server.

- For a complete guide on installing OpenDID, please refer to [Open DID Installation Guide].
- For guidance on the Admin console, please refer to [Open DID Admin Console Guide].

<br/>

## 1.2. Verifier Server Definition

The Verifier server provides Verifiable Presentation (VP) verification APIs in Open DID.<br>
The Verifier server provides APIs such as Request Profile and Request Verify that are performed for verification.
<br/>

## 1.3. System Requirements

- **Java 17** or higher
- **Gradle 7.0** or higher
- **Docker** and **Docker Compose** (when using Docker)
- Minimum **2GB RAM** and **10GB disk space**

<br/>

# 2. Prerequisites

This chapter introduces the prerequisites needed before installing the components of the Open DID project.

## 2.1. Git Installation

`Git` is a distributed version control system that tracks changes to source code and supports collaboration between multiple developers. Git is essential for managing and versioning the source code of the Open DID project.

After successful installation, you can check the Git version using the following command:

```bash
git --version
```

> **Reference Links**
>
> - [Git Installation Guide](https://docs.github.com/en/repositories/creating-and-managing-repositories/cloning-a-repository)

<br/>

## 2.2. PostgreSQL Installation

To run the Verifier server, a database installation is required, and Open DID uses PostgreSQL.

> **Reference Links**

- [PostgreSQL Installation Guide Document](https://www.postgresql.org/download/)
- [8. Installing Docker PostgreSQL](#8-installing-docker-postgresql)

<br/>

## 2.3. Node.js Installation
To run the React-based Verifier Admin Console, `Node.js` and `npm` are required.

npm (Node Package Manager) is used to install and manage dependencies needed for frontend development.

After installation is complete, you can verify that it has been installed correctly with the following commands:

```bash
node --version
npm --version
```

> **Reference Links**  
> - [Node.js Official Download Page](https://nodejs.org/)  
> - It is recommended to install the LTS (Long Term Support) version.  

> ✅ Installation Verification Tip  
> If version information is displayed when you enter the `node -v` and `npm -v` commands, the installation is successful.

# 3. Cloning Source Code from GitHub

## 3.1. Clone Source Code

The `git clone` command is used to clone source code from a remote repository hosted on GitHub to your local computer. This command allows you to work with the project's entire source code and related files locally. After cloning, you can work within the repository and push changes back to the remote repository.

Open a terminal and run the following commands to copy the Verifier server repository to your local computer:

```bash
# Clone repository from Git repository
git clone https://github.com/OmniOneID/did-verifier-server.git

# Navigate to the cloned repository
cd did-verifier-server
```

> **Reference Links**
>
> - [Git Clone Guide](https://docs.github.com/en/repositories/creating-and-managing-repositories/cloning-a-repository)

<br/>

## 3.2. Directory Structure

The main directory structure of the cloned project is as follows:

```
did-verifier-server
├── CHANGELOG.md
├── CLA.md
├── CODE_OF_CONDUCT.md
├── CONTRIBUTING.md
├── LICENSE
├── dependencies-license.md
├── MAINTAINERS.md
├── README.md
├── RELEASE-PROCESS.md
├── SECURITY.md
├── docs
│   └── api
│       └── Verifier_API_ko.md
│   └── errorCode
│       └── Verifier_ErrorCode.md
│   └── installation
│       └── OpenDID_VerifierServer_InstallationAndOperation_Guide.md
│       └── OpenDID_VerifierServer_InstallationAndOperation_Guide_ko.md
│   └── db
│       └── OpenDID_TableDefinition_Verifier.md
└── source
    └── did-verifier-server
        ├── gradle
        ├── libs
            └── did-sdk-common-1.0.0.jar
            └── did-blockchain-sdk-server-1.0.0.jar
            └── did-core-sdk-server-1.0.0..jar
            └── did-crypto-sdk-server-1.0.0.jar
            └── did-datamodel-server-1.0.0.jar
            └── did-wallet-sdk-server-1.0.0.jar
        ├── sample
        └── src
        └── build.gradle
        └── README.md
    └── did-verifier-admin 
```

| Name                    | Description                              |
| ----------------------- | ---------------------------------------- |
| CHANGELOG.md            | Version-specific changes of the project  |
| CODE_OF_CONDUCT.md      | Code of conduct for contributors         |
| CONTRIBUTING.md         | Contribution guidelines and procedures   |
| LICENSE                 | Library license information              |
| dependencies-license.md | License information for project dependencies |
| MAINTAINERS.md          | Guidelines for project maintainers       |
| RELEASE-PROCESS.md      | Procedures for releasing new versions    |
| SECURITY.md             | Security policies and vulnerability reporting methods |
| docs                    | Documentation                            |
| ┖ api                   | API guide documents                      |
| ┖ errorCode             | Error codes and troubleshooting guides   |
| ┖ installation          | Installation and setup guides            |
| ┖ db                    | Database ERD, table specifications       |
| source/did-verifier-server| Verifier server source code and build files |
| ┖ gradle                | Gradle build settings and scripts        |
| ┖ libs                  | External libraries and dependencies      |
| ┖ sample                | Sample files                             |
| ┖ src                   | Main source code directory               |
| ┖ build.gradle          | Gradle build configuration file          |
| ┖ README.md             | Source code overview and guide           |
| source/did-verifier-admin| Verifier Admin console source code      |

<br/>

# 4. Server Deployment Methods

This chapter guides you through three methods of deploying the server.

The project source is located under the `source` directory, and you need to configure the source according to each deployment method.

1. **Using an IDE**: You can open the project in an integrated development environment (IDE), configure run settings, and run the server directly. This method is useful for quickly testing code changes during development.

2. **Using console commands after building**: You can build the project and then run the generated JAR file using console commands (`java -jar`) to deploy the server. This method is primarily used when deploying the server or running it in a production environment.

3. **Building with Docker**: You can build the server as a Docker image and run it as a Docker container. This method has the advantage of maintaining consistency between environments and facilitating deployment and scaling.

## 4.1. Running with IDE (Gradle and React Projects)

The Open DID project consists of a backend (Spring Boot-based) and a frontend (React-based), which can be developed and run in IntelliJ IDEA and VS Code, respectively.

### 4.1.1. Running Backend (Spring Boot) with IntelliJ IDEA

IntelliJ IDEA is a widely used IDE for Java development that is compatible with Gradle-based projects. Since the Open DID server uses Gradle, it can be easily run in IntelliJ.

#### 1. Install IntelliJ IDEA

- [Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

#### 2. Open Project

- Select `File -> New -> Project from Existing Sources`  
- Select the `source/did-verifier-server` directory  
- The `build.gradle` file will be automatically recognized, and necessary dependencies will be downloaded

#### 3. Gradle Build

- Run `Tasks -> build -> build` in the `Gradle` tab

#### 4. Run Server

- Run `Tasks -> application -> bootRun`  
- The server is running normally when the console displays the message `"Started [ApplicationName] in [time] seconds"`

> ⚠️ Runs with the default `sample` profile. Runs as a test without a database  
> For more details, refer to [6. Profile Configuration and Usage](#6-profile-configuration-and-usage)

#### 5. Database Installation

- Use PostgreSQL (Docker installation recommended)  
- For detailed installation methods, refer to [2.2. PostgreSQL Installation](#22-postgresql-installation)

#### 6. Server Configuration

- Configuration file location: `src/main/resources/config`  
- Examples: DB connection information, port, email settings, etc.  
- For detailed configuration methods, refer to [5. Configuration Guide](#5-configuration-guide)

---

### 4.1.2. Running Frontend (React) with VS Code

The Verifier admin console is React-based and can be run separately in VS Code. This is useful for frontend development or UI checking.

#### 1. Install VS Code

- [Download VS Code](https://code.visualstudio.com/)

#### 2. Open Project

- Open the `source/did-verifier-admin` directory in VS Code

#### 3. Install Dependencies

```bash
npm install
```

#### 4. Run Development Server

```bash
npm run dev
```

- Default access URL: [http://localhost:5173](http://localhost:5173)

> 📌 **Note:**  
> The backend (Spring Boot server) must be running separately, and  
> the API server address in the frontend can be specified through the `vite.config.ts` file or configuration file.
   

<br/>


## 4.2. Running with Console Commands

This section guides you on how to run the Open DID server using console commands. It explains the process of building the project using Gradle and running the server using the generated JAR file.
- When building with Gradle, the frontend (Admin Console) is automatically built together and included as static resources.

### 4.2.1. Gradle Build Commands

- Build the source using gradlew.
  ```shell
    # Navigate to the source folder of the cloned repository
    cd source/did-verifier-server

    # Grant execution permission to Gradle Wrapper
    chmod 755 ./gradlew

    # Clean build the project (delete previous build files and build anew)
    ./gradlew clean build
  ```
  > Note: If frontend build is not needed (e.g., when testing only the backend or already including frontend build results), you can add the following option to skip the frontend build: 
  > - `./gradlew clean build -DskipFrontendBuild=true`


- Navigate to the built folder and verify that the JAR file has been created.
    ```shell
      cd build/libs
      ls
    ```
- This command will create a `did-verifier-server-1.0.0.jar` file.

<br/>

## 4.3. Running with Docker

- For Docker image building, configuration, execution, etc., please refer to [7. Building and Running with Docker](#7-building-and-running-with-docker) below.

<br/>

# 5. Configuration Guide

This chapter provides guidance on all configuration values included in the server's configuration files. Each configuration is an important element that controls the server's behavior and environment, requiring appropriate settings for stable server operation. Please refer to the item-by-item descriptions and examples to apply the appropriate settings for each environment.

Please note that settings with the 🔒 icon are fixed values by default or generally do not need to be modified.

## 5.1. application.yml

- Role: The application.yml file defines the basic settings of a Spring Boot application. Through this file, you can specify various environment variables such as application name, database settings, profile settings, etc., which have a significant impact on how the application operates.

- Location: `src/main/resources/`

### 5.1.1. Spring Basic Configuration

The basic Spring configuration defines the application name and active profiles, playing an important role in setting the server's operating environment.

- `spring.application.name`: 🔒
  - Specifies the name of the application.
  - Purpose: Mainly used for identifying the application in log messages, monitoring tools, or Spring Cloud services.
  - Example: `Verifier`

- `spring.profiles.active`:  
  - Defines the profile to activate.
  - Purpose: Choose between sample or development environment to load settings appropriate for that environment. For more details on profiles, please refer to chapter [6. Profile Configuration and Usage](#6-profile-configuration-and-usage).
  - Supported profiles: sample, dev
  - Example: `sample`, `dev`

- `spring.profiles.group.dev`: 🔒
  - Defines individual profiles included in the `dev` profile group.
  - Purpose: Groups and manages settings to be used in the development environment.
  - Profile file naming convention: Configuration files for each profile use the name as defined in the group. For example, the auth profile uses application-auth.yml, and the databases profile uses application-databases.yml. You must use the filename exactly as written under group.dev.

- `spring.profiles.group.sample`: 🔒
  - Defines individual profiles included in the `sample` profile group.
  - Purpose: Groups and manages settings to be used in the development environment.
  - Profile file naming convention: Configuration files for each profile use the name as defined in the group. For example, the auth profile uses application-auth.yml, and the databases profile uses application-databases.yml. You must use the filename exactly as written under group.sample.

<br/>

### 5.1.2. Jackson Basic Configuration

Jackson is the default JSON serialization/deserialization library used in Spring Boot. Through Jackson's configuration, you can adjust the serialization method or format of JSON data, improving performance and efficiency during data transmission.

- `spring.jackson.default-property-inclusion`: 🔒
  - Sets to not serialize property values when they are null.
  - Example: non_null

- `spring.jackson.fail-on-empty-beans`: 🔒
  - Sets to not generate an error when serializing empty objects.
  - Example: false

<br/>

### 5.1.3. Server Configuration

Server configuration defines the port number on which the application will receive requests.

- `server.port`:  
  - The port number on which the application will run. The default port for the Verifier server is 8092.
  - Value: 8092

<br/>

### 5.1.4. TAS Configuration

The Verifier service communicates with the TAS server. You should configure the address of the TAS server you have directly built.

- `tas.url`:  
  - The URL of the TAS (Trust Anchor Service) service. It can be used for authentication or trust verification.
  - Example: `http://localhost:8090/contextpath/tas`

<br/>

## 5.3. database.yml

- Role: Defines how to manage and operate databases in the server, from database connection information to migration settings using Liquibase and JPA settings.

- Location: `src/main/resources/`
  
### 5.3.1. Spring Liquibase Configuration

Liquibase is a tool for managing database migrations, helping to track changes to database schemas and apply them automatically. This helps maintain database consistency in development and production environments.

- `spring.liquibase.change-log`: 🔒
  - Specifies the location of the database change log file. This is the log file used by Liquibase to track and apply database schema changes.
  - Example: `classpath:/db/changelog/master.xml`

- `spring.liquibase.enabled`: 🔒
  - Sets whether to activate Liquibase. When set to true, Liquibase runs when the application starts and performs database migration. The `sample` profile does not connect to a database, so it should be set to false.
  - Example: `true` [dev], `false` [sample]

- `spring.liquibase.fall-on-error`: 🔒
  - Controls the behavior when an error occurs during Liquibase database migration. Only set in the `sample` profile.
  - Example: `false` [sample]

<br/>

### 5.3.2. Datasource Configuration

Datasource configuration defines basic information for the application to connect to the database. This includes database driver, URL, username, and password information.

- `spring.datasource.driver-class-name`: 🔒
  - Specifies the database driver class to use. Specifies the JDBC driver for connecting to the database.
  - Example: `org.postgresql.Driver`

- `spring.datasource.url`:  
  - The database connection URL. Specifies the location and name of the database to which the application will connect.
  - Example: `jdbc:postgresql://localhost:5432/verifier_db`

- `spring.datasource.username`:  
  - The username for database access.
  - Example: `verifier`

- `spring.datasource.password`:  
  - The password for database access.
  - Example: `verifierpassword`

<br/>

### 5.3.3. JPA Configuration

JPA configuration controls how the application interacts with the database and has a significant impact on performance and readability.

- `spring.jpa.open-in-view`: 🔒
  - Sets whether to use the OSIV (Open Session In View) pattern. When set to true, it maintains database connections for the entire HTTP request.
  - Example: `true`

- `spring.jpa.show-sql`: 🔒
  - Sets whether to log SQL queries. When set to true, it outputs executed SQL queries to the log. Useful for debugging during development.
  - Example: `true`

- `spring.jpa.hibernate.ddl-auto`: 🔒
  - Sets the automatic DDL generation mode for Hibernate. Specifies the automatic schema generation strategy for the database. When set to 'none', automatic generation is disabled.
  - Example: `none`

- `spring.jpa.hibernate.naming.physical-strategy`: 🔒
  - Sets the naming strategy for database objects. Specifies the strategy for converting entity class names to database table names.
  - Example: `org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy`

- `spring.jpa.properties.hibernate.format_sql`: 🔒
  - Sets whether to format SQL. When set to false, it disables formatting of SQL queries output to the log.
  - Example: `false`

<br/>

## 5.4. application-logging.yml

- Role: Configures log groups and log levels. Through this configuration file, you can define log groups for specific packages or modules and individually specify log levels for each group.

- Location: `src/main/resources/`
  
### 5.4.1. Logging Configuration

- Log Groups: You can group and manage desired packages under logging.group. For example, include the org.omnione.did.base.util package in the util group, and define other packages as separate groups.

- Log Levels: Through the logging.level setting, you can specify the log level for each group. You can set various log levels such as debug, info, warn, error to output logs at the desired level.

- `logging.level`:
  - Sets the log level.
  - By setting the level to debug, you can see all log messages at DEBUG level and above (INFO, WARN, ERROR, FATAL) for the specified package.

Complete example:

```yaml
logging:
  level:
    org.omnione: debug
```

<br/>

## 5.5. application-spring-docs.yml

- Role: Manages SpringDoc and Swagger UI settings in the application.

- Location: `src/main/resources/`

- `springdoc.swagger-ui.path`: 🔒
  - Defines the URL path for accessing Swagger UI.
  - Example: `/swagger-ui.html`

- `springdoc.swagger-ui.groups-order`: 🔒
  - Specifies the order in which API groups are displayed in Swagger UI.
  - Example: `ASC`

- `springdoc.swagger-ui.operations-sorter`: 🔒
  - Sorts API endpoints in Swagger UI based on HTTP method.
  - Example: `method`

- `springdoc.swagger-ui.disable-swagger-default-url`: 🔒
  - Disables the default Swagger URL.
  - Example: `true`

- `springdoc.swagger-ui.display-request-duration`: 🔒
  - Sets whether to display the request time in Swagger UI.
  - Example: `true`

- `springdoc.api-docs.path`: 🔒
  - Defines the path where API documentation is provided.
  - Example: `/api-docs`

- `springdoc.show-actuator`: 🔒
  - Sets whether to display Actuator endpoints in the API documentation.
  - Example: `true`

- `springdoc.default-consumes-media-type`: 🔒
  - Sets the default media type for request bodies in the API documentation.
  - Example: `application/json`

- `springdoc.default-produces-media-type`: 🔒
  - Sets the default media type for response bodies in the API documentation.
  - Example: `application/json`

<br/>

## 5.6. application-wallet.yml

- Role: Configures wallet file information used by the server.

- Location: `src/main/resources/`

- `wallet.file-path`:  
  - Specifies the path to the wallet file. Specifies the location of the file storing the file wallet. This file may contain important information such as private keys. *Must be entered as an absolute path*.
  - Example: `/path/to/your/verifier.wallet`

- `wallet.password`:  
  - The password used to access the wallet. This is the password used to access the wallet file. It is information requiring high security.
  - Example: `your_secure_wallet_password`

## 5.7. application-verifier.yml

This configuration file defines the basic information for the Verifier server and VP policy Offer expiration time.

- `verifier.name`:
  - Specifies the name of the Verifier server. This value is used as the value of name in the membership certificate VC.
  - Example: raonsecure

- `verifier.did`:
  - Sets the DID for the Verifier server.
  - Example: did:omn:verifier

- `verifier.certificate-vc`:
  - Specifies the address (URL) where the Verifier's registration certificate (VC) can be accessed.
  - This URL allows verification of the authenticity of certificates issued by the Issuer.
  - Format: {Verifier domain}/verifier/api/v1/certificate-vc
  - Example: <http://127.0.0.1:8092/verifier/api/v1/certificate-vc>

- `verifier.cipher-type:`: 🔒
  - Specifies the encryption algorithm to be used by the Verifier server.
  - Example: AES-256-CBC

- `verifier.padding-type:`: 🔒
  - Specifies the padding method to use in encryption.
  - Example: PKCS5

- `verifier.token-expiration-time-hours:`:
  - Sets the expiration time of authentication tokens in hours.
  - Example: 1

- `verifier.sample-path`:
  - Description: Sets the path to store sample data. The sample folder is located in the root path of the source folder.
    The Verifier server provides two sample files for arbitrary VP policies for sample and demo implementation. These are for reference and are not specifications (Out of scope).
  - Example: ./source/did-verifier-server/sample/data/vpPolicy

- `verifier.valid-seconds`:
  - Description: The validity period (in seconds) for QR codes when requesting a VP offer. The default value is 180 seconds.
  - Example: 180

<br/>

## 5.8. VP policy

This section explains examples and configurations for VpPolicy files. While there are no regulations on the form or storage method of these files, they have been stored for offer request implementation and demonstration. For the data structure and main content, please refer to the data specification. The address values below need to be modified to match your server.

- `policdyId`: An ID for VpPolicy with no regulated data format.
- `payload`: The payload provided during the Verifier's offer request. Refer to the data specification (4.6.7.1. VerifyOfferPayload) and the Presentation of VP_ko.md file.
- `profile`: The Verifier's profile. For details, refer to the data specification (4.5.2. VerifyProfile).

```json
//Example
{
  "policyId": "99999-9992",
  "payload" : {
    "device": "WEB",
    "service": "signup",
    "endpoints": [      
      "http://{verifier_domain}:8092/verifier"
    ],
    "locked": false,
    "mode": "Direct"
  },
  "profile": {
    "id": "",
    "type": "VerifyProfile",
    "title": "OpenDID Registration VP Profile",
    "description": "Profile for VPs required for OpenDID registration submission.",
    "encoding": "UTF-8",
    "language": "ko",
    "profile": {
      "verifier": {
        "did": "did:omn:verifier",
        // Verifier Cert VC URL
        "certVcRef": "http://{verifier_domain}:8092/verifier/api/v1/certificate-vc",
        "name": "verifier",
        "description": "verifier",
        "ref": "http://{verifier_domain}:8092/verifier/api/v1/certificate-vc"
      },
      "filter": {
        "credentialSchemas": [
          {
            //Issuer Server's domain
            "id": "http://{issuer_domain}:8091/issuer/api/v1/vc/vcschema?name=mdl",
            "type": "OsdSchemaCredential",
            "requiredClaims": [
              "org.iso.18013.5.birth_date",
              "org.iso.18013.5.family_name",
              "org.iso.18013.5.given_name"
            ],
            "allowedIssuers":[
              "did:omn:issuer"
            ],
            "displayClaims":[
              "testId.aa"
            ],
            "value": "VerifiableProfile"
          }
        ]
      },
      "process": {
        "endpoints": [
          "http://{verifier_domain}:8092/verifier"
        ],
        "reqE2e": {
          "nonce": "",
          "curve": "Secp256r1",
          "publicKey": "",
          "cipher": "AES-256-CBC",
          "padding": "PKCS5"
        },
        "verifierNonce": "",
        "authType": 0
      }
    }
  }
}
```

## 5.9. blockchain.properties

- Role: Configures the blockchain server information to be linked with the Verifier server. According to '5.1.1. Installing Hyperledger Fabric Test Network' in the [Open DID Installation Guide], when you install the Hyperledger Fabric test network, configuration files for private keys, certificates, and server connection information are automatically generated. In blockchain.properties, you configure the path where these files are located and the network name you entered during the Hyperledger Fabric test network installation. You also set the Open DID chaincode name deployed in '5.1.2. Deploying Open DID Chaincode'.

- Location: `src/main/resources/properties`

### 5.9.1. Blockchain Integration Configuration

- `fabric.configFilePath:`:
  - Sets the path where the Hyperledger Fabric connection information file is located. This file is automatically generated during Hyperledger Fabric test network installation, and the default filename is 'connection-org1.json'.
  - Example: {yourpath}/connection-org1.json

- `fabric.privateKeyFilePath:`:
  - Sets the path to the private key file used by the Hyperledger Fabric client for transaction signatures and authentication on the network. This file is automatically generated during Hyperledger Fabric test network installation.
  - Example: {yourpath}/{private key filename}

- `fabric.certificateFilePath:`:
  - Sets the path where the Hyperledger Fabric client certificate is located. This file is automatically generated during Hyperledger Fabric test network installation, and the default filename is 'cert.pem'.
  - Example: /{yourpath}/cert.pem

- `fabric.mychannel:`:
  - The name of the private network (channel) used in Hyperledger Fabric. You must set the channel name that you entered during Hyperledger Fabric test network installation.
  - Example: mychannel

- `fabric.chaincodeName:`: 🔒
  - The name of the Open DID chaincode used in Hyperledger Fabric. This value is fixed as 'opendid'.
  - Example: opendid

<br/>

# 6. Profile Configuration and Usage

## 6.1. Profile Overview (`sample`, `dev`)

The Verifier server supports two profiles, `dev` and `sample`, to run in various environments.

Each profile is designed to apply settings appropriate for that environment. By default, the Verifier server is configured with the `sample` profile, which is designed to run the server independently without integration with external services such as databases or blockchain. The `sample` profile is suitable for API call testing, allowing developers to quickly check the basic operation of the application. This profile returns fixed response data for all API calls, making it useful in initial development environments.

Sample API calls are written as JUnit tests, which can be referenced when writing tests.

On the other hand, the `dev` profile is designed to perform actual operations. Using this profile allows testing and validation of real data. When the `dev` profile is activated, it integrates with external services such as real databases and blockchain, allowing you to test the application's behavior in a real environment.

### 6.1.1. `sample` Profile
The `sample` profile is designed to run the server independently without integration with external services (DB, blockchain, etc.). This profile is suitable for API call testing, allowing developers to quickly check the basic operation of the application. It returns fixed response data for all API calls, making it useful in the initial development stage or for functional testing. As it doesn't require any integration with external systems, it provides an environment where the server can be run and tested standalone.
> Note: When using the sample profile, the Admin Console does not work.

### 6.1.2. `dev` Profile

The `dev` profile includes settings suitable for the development environment and is used on development servers. To use this profile, settings for the development environment's database and blockchain nodes are required.

## 6.2. Profile Configuration Methods

This section explains how to change the profile for each deployment method.

### 6.2.1. When Running the Server Using an IDE

- **Select Configuration File:** Select the `application.yml` file in the `src/main/resources` path.
- **Specify Profile:** Add the `--spring.profiles.active={profile}` option in the IDE's run settings (Run/Debug Configurations) to activate the desired profile.
- **Apply Settings:** The corresponding configuration file is applied according to the activated profile.

### 6.2.2. When Running the Server Using Console Commands

- **Select Configuration File:** Prepare profile-specific configuration files in the same directory as the built JAR file or in the path where configuration files are located.
- **Specify Profile:** Add the `--spring.profiles.active={profile}` option to the server startup command to activate the desired profile.
  
  ```bash
  java -jar build/libs/did-verifier-server-1.0.0.jar --spring.profiles.active={profile}
  ```

- **Apply Settings:** The corresponding configuration file is applied according to the activated profile.

### 6.2.3. When Running the Server Using Docker

- **Select Configuration File:** When creating a Docker image, specify the configuration file path in the Dockerfile or mount an external configuration file to the Docker container.
- **Specify Profile:** Set the `SPRING_PROFILES_ACTIVE` environment variable in the Docker Compose file or Docker run command to specify the profile.
  
  ```yaml
  environment:
    - SPRING_PROFILES_ACTIVE={profile}
  ```

- **Apply Settings:** Settings are applied according to the profile specified when the Docker container runs.

You can flexibly change profile-specific settings according to each method and easily apply settings appropriate for your project environment.

# 7. Building and Running with Docker

## 7.1. Building Docker Image (`Dockerfile` Based)

Build a Docker image with the following command:

```bash
docker build -t did-verifier-server .
```

## 7.2. Running Docker Image

Run the built image:

```bash
docker run -d -p 8092:8092 did-verifier-server
```

## 7.3. Running with Docker Compose

### 7.3.1. `docker-compose.yml` File Description

You can easily manage multiple containers using a `docker-compose.yml` file.

```yaml
version: '3'
services:
  app:
    image: did-verifier-server
    ports:
      - "8092:8092"
    volumes:
      - ${your-config-dir}:/app/config
    environment:
      - SPRING_PROFILES_ACTIVE=local
```

### 7.3.2. Container Execution and Management

Run containers using Docker Compose with the following command:

```bash
docker-compose up -d
```

### 7.3.3. Server Configuration Method

In the example above, the `${your-config-dir}` directory is mounted to `/app/config` inside the container to share configuration files.

- If additional configuration is needed, you can add separate property files to the mounted folder to change settings.
  - For example, add an `application.yml` file to `${your-config-dir}` and write the settings you want to change in this file.
  - The `application.yml` file located in `${your-config-dir}` takes precedence over the default configuration file.
- For detailed configuration methods, please refer to [5. Configuration Guide](#5-configuration-guide).

# 8. Installing Docker PostgreSQL

This section explains how to install PostgreSQL using Docker. Using this method, you can easily install PostgreSQL and link it to the server.

## 8.1. Installing PostgreSQL with Docker Compose

Here's how to install PostgreSQL using Docker Compose.

```yml
services:
  postgres:
    container_name: postgre-verifier
    image: postgres:16.4
    restart: always
    volumes:
      - postgres_data_verifier:/var/lib/postgresql/data
    ports:
      - 5432:5432
    environment:
      POSTGRES_USER: ${USER}
      POSTGRES_PASSWORD: ${PW}
      POSTGRES_DB: verifier

volumes:
  postgres_data_verifier:
```

This Docker Compose file installs PostgreSQL version 16.4 and makes the following settings:

- **container_name**: Sets the container name to `postgre-verifier`.
- **volumes**: Mounts the `postgres_data_verifier` volume to PostgreSQL's data directory (`/var/lib/postgresql/data`). This ensures that data is permanently preserved.
- **ports**: Maps port 5432 on the host to port 5432 in the container.
- **environment**: Sets PostgreSQL's username, password, and database name. Here, `${USER}` and `${PW}` can be set as environment variables.

## 8.2. Running PostgreSQL Container

To run the PostgreSQL container using the Docker Compose file above, run the following command in the terminal:

```bash
docker-compose up -d
```

This command runs the PostgreSQL container in the background. The PostgreSQL server runs according to the environment variables set, and the database is prepared. You can proceed with the connection settings to use this database in your application.

[Open DID Installation Guide]: https://github.com/OmniOneID/did-release/blob/main/release-V1.0.0.0/OepnDID_Installation_Guide-V1.0.0.0.md
[Open DID Admin Console Guide]: https://github.com/OmniOneID/did-release/blob/main/release-V1.0.0.0/