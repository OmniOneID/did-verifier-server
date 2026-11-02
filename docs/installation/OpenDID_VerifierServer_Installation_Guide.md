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

Open DID Verifier Server Installation Guide
==

- Date: 2026-11-02
- Version: v3.0.0

Table of Contents
==

- [Open DID Verifier Server Installation Guide](#open-did-verifier-server-installation-guide)
- [Table of Contents](#table-of-contents)
- [1. Introduction](#1-introduction)
  - [1.1. Overview](#11-overview)
  - [1.2. Verifier Server Definition](#12-verifier-server-definition)
  - [1.3. System Requirements](#13-system-requirements)
- [2. Prerequisites](#2-prerequisites)
  - [2.1. Git Installation](#21-git-installation)
  - [2.2. PostgreSQL Installation](#22-postgresql-installation)
  - [2.3. Node.js Installation](#23-nodejs-installation)
- [3. Cloning Source Code from GitHub](#3-cloning-source-code-from-github)
  - [3.1. Source Code Clone](#31-source-code-clone)
  - [3.2. Directory Structure](#32-directory-structure)
- [4. Server Running Methods](#4-server-running-methods)
  - [4.1. Running with IDE (Gradle and React Project Execution)](#41-running-with-ide-gradle-and-react-project-execution)
    - [4.1.1. Running Backend (Spring Boot) in IntelliJ IDEA](#411-running-backend-spring-boot-in-intellij-idea)
      - [1. Install IntelliJ IDEA](#1-install-intellij-idea)
      - [2. Open Project](#2-open-project)
      - [3. Gradle Build](#3-gradle-build)
      - [4. Run Server](#4-run-server)
      - [5. Database Installation](#5-database-installation)
      - [6. Server Configuration](#6-server-configuration)
    - [4.1.2. Running Frontend (React) in VS Code](#412-running-frontend-react-in-vs-code)
      - [1. Install VS Code](#1-install-vs-code)
      - [2. Open Project](#2-open-project-1)
      - [3. Install Dependencies](#3-install-dependencies)
      - [4. Run Development Server](#4-run-development-server)
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
  - [5.7. blockchain.properties](#57-blockchainproperties)
    - [5.7.1. Blockchain Integration Configuration](#571-blockchain-integration-configuration)
      - [EVM Network Configuration](#evm-network-configuration)
      - [EVM Contract Configuration](#evm-contract-configuration)
  - [5.8. application-verifier.yml (Status List)](#58-application-verifieryml-status-list)
  - [5.9. application-lss.yml](#59-application-lssyml)
  - [5.10. OID4VP Configuration](#510-oid4vp-configuration)
- [6. Profile Configuration and Usage](#6-profile-configuration-and-usage)
  - [6.1. Profile Overview (`sample`, `dev`, `lss`)](#61-profile-overview-sample-dev-lss)
    - [6.1.1. `sample` Profile](#611-sample-profile)
    - [6.1.2. `dev` Profile](#612-dev-profile)
    - [6.1.3. `lss` Profile](#613-lss-profile)
  - [6.2. Profile Configuration Methods](#62-profile-configuration-methods)
    - [6.2.1. When Running Server with IDE](#621-when-running-server-with-ide)
    - [6.2.2. When Running Server with Console Commands](#622-when-running-server-with-console-commands)
    - [6.2.3. When Running Server with Docker](#623-when-running-server-with-docker)
- [7. Running After Building with Docker](#7-running-after-building-with-docker)
  - [7.1. Docker Image Build Method (Based on `Dockerfile`)](#71-docker-image-build-method-based-on-dockerfile)
    - [7.1.1. Build Docker image](#711-build-docker-image)
  - [7.2. Running with Docker Compose](#72-running-with-docker-compose)
    - [7.2.1. Preparing Directories and Configuration Files](#721-preparing-directories-and-configuration-files)
      - [1. Create docker-compose directory and config directory](#1-create-docker-compose-directory-and-config-directory)
      - [2. Copy configuration files (yml) to config directory](#2-copy-configuration-files-yml-to-config-directory)
      - [3. Modify blockchain.properties file](#3-modify-blockchainproperties-file)
      - [4. Modify application-database.yml file](#4-modify-application-databaseyml-file)
    - [7.2.2. Create `docker-compose.yml` file](#722-create-docker-composeyml-file)
    - [7.2.3. Run Container](#723-run-container)
- [8. Installing Docker PostgreSQL](#8-installing-docker-postgresql)
  - [8.1. PostgreSQL Installation using Docker Compose](#81-postgresql-installation-using-docker-compose)
  - [8.2. PostgreSQL Container Execution](#82-postgresql-container-execution)

# 1. Introduction

## 1.1. Overview

This document provides a guide for installation, configuration, and operation of the Open DID Verifier server. The Verifier server consists of a Spring Boot-based backend and a React-based Admin console frontend, with integrated deployment possible through Gradle builds. It provides step-by-step instructions for installation processes, environment configuration, Docker execution methods, and profile settings, enabling users to efficiently install and run the server.

- For a complete installation guide of OpenDID, please refer to [Open DID Installation Guide].
- For the Admin console guide, please refer to [Open DID Admin Console Guide].

<br/>

## 1.2. Verifier Server Definition

The Verifier server provides Verifiable Presentation (VP) verification APIs in Open DID.<br>
The Verifier server provides APIs such as Request Proof Request and Proof Verify for ZKP verification, and Request Profile and Request Verify for general VP submission.
<br/>

## 1.3. System Requirements

- **Java 21** or higher
- **Gradle 7.0** or higher
- **Docker** and **Docker Compose** (when using Docker)
- Minimum **2GB RAM** and **10GB disk space**

<br/>

# 2. Prerequisites

This chapter guides you through the prerequisites needed before installing the components of the Open DID project.

## 2.1. Git Installation

`Git` is a distributed version control system that tracks changes in source code and supports collaboration among multiple developers. Git is essential for managing the source code of the Open DID project and version control.

Once the installation is successful, you can check Git's version using the following command:

```bash
git --version
```

> **Reference Links**
>
> - [Git Installation Guide](https://docs.github.com/en/repositories/creating-and-managing-repositories/cloning-a-repository)

<br/>

## 2.2. PostgreSQL Installation

To run the Verifier server, database installation is required, and Open DID uses PostgreSQL.

> **Reference Links**

- [PostgreSQL Installation Guide Documentation](https://www.postgresql.org/download/)
- [8. Installing Docker PostgreSQL](#8-installing-docker-postgresql)

<br/>

## 2.3. Node.js Installation
To run the React-based Verifier Admin Console, `Node.js` and `npm` are required.

npm (Node Package Manager) is used to install and manage dependencies needed for frontend development.

Once the installation is complete, you can verify proper installation with the following commands:

```bash
node --version
npm --version
```

> **Reference Links**  
> - [Node.js Official Download Page](https://nodejs.org/)  
> - LTS (Long Term Support) version installation is recommended.  

> ✅ Installation Verification Tip  
> If version information is displayed when you enter `node -v` and `npm -v` commands, the installation was successful.

# 3. Cloning Source Code from GitHub

## 3.1. Source Code Clone

The `git clone` command clones source code from a remote repository hosted on GitHub to your local computer. Using this command, you can work with the entire project source code and related files locally. After cloning, you can proceed with necessary work within the repository, and changes can be pushed back to the remote repository.

Open a terminal and run the following commands to copy the Verifier server repository to your local computer.

```bash
# Clone repository from Git repository
git clone https://github.com/OmniOneID/did-verifier-server.git

# Move to the cloned repository
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
├── README_ko.md
├── RELEASE-PROCESS.md
├── SECURITY.md
├── docs
│   ├── admin
│   │   ├── OpenDID_VerifierAdmin_Operation_Guide.md
│   │   └── OpenDID_VerifierAdmin_Operation_Guide_ko.md
│   ├── api
│   │   ├── Verifier_API.md
│   │   └── Verifier_API_ko.md
│   ├── db
│   │   ├── OpenDID_TableDefinition_Verifier.md
│   │   └── OpenDID_TableDefinition_Verifier_ko.md
│   ├── errorCode
│   │   ├── Verifier_ErrorCode.md
│   │   └── Verifier_ErrorCode_ko.md
│   └── installation
│       ├── OpenDID_VerifierServer_Installation_Guide.md
│       └── OpenDID_VerifierServer_Installation_Guide_ko.md
└── source
    ├── Dockerfile
    ├── did-verifier-admin
    └── did-verifier-server
        ├── gradle
        ├── libs
        │   ├── did-blockchain-sdk-server-2.0.0.jar
        │   ├── did-common-sdk-server-3.0.0.jar
        │   ├── did-core-sdk-server-2.0.0.jar
        │   ├── did-crypto-sdk-server-2.0.0.jar
        │   ├── did-datamodel-sdk-server-2.0.0.jar
        │   ├── did-oid4vp-sdk-server-3.0.0.jar
        │   ├── did-verifier-sdk-server-1.0.0.jar
        │   ├── did-wallet-sdk-server-2.0.0.jar
        │   └── did-zkp-sdk-server-2.0.1.jar
        ├── sample
        ├── src
        ├── build.gradle
        └── README.md
```

| Name                    | Description                              |
| ----------------------- | ---------------------------------------- |
| CHANGELOG.md            | Version-specific changes of the project |
| CODE_OF_CONDUCT.md      | Code of conduct for contributors         |
| CONTRIBUTING.md         | Contribution guidelines and procedures   |
| LICENSE                 | Library license information |
| dependencies-license.md | License information for project dependency libraries |
| MAINTAINERS.md          | Guidelines for project maintainers      |
| RELEASE-PROCESS.md      | Procedures for releasing new versions    |
| SECURITY.md             | Security policies and vulnerability reporting methods |
| docs                    | Documentation                            |
| ┖ admin                 | Admin guide documentation                |
| ┖ api                   | API guide documentation                  |
| ┖ errorCode             | Error codes and troubleshooting guides  |
| ┖ installation          | Installation and configuration guides   |
| ┖ db                    | Database ERD, table specifications      |
| source/did-verifier-server| Verifier server source code and build files |
| ┖ gradle                | Gradle build settings and scripts       |
| ┖ libs                  | External libraries and dependencies      |
| ┖ sample                | Sample files                             |
| ┖ src                   | Main source code directory              |
| ┖ build.gradle          | Gradle build configuration file         |
| ┖ README.md             | Source code overview and guide          |
| source/did-verifier-admin| Verifier Admin console source code     |

<br/>

# 4. Server Running Methods

This chapter guides you through three methods to run the server.

Project sources are located under the `source` directory, and you need to load and configure sources from the corresponding directory depending on each running method.

1. **Using IDE**: You can open the project in an Integrated Development Environment (IDE), set up execution configurations, and run the server directly. This method is useful for quickly testing code changes during development.

2. **Using console commands after Build**: After building the project, you can run the server by executing the generated JAR file with console commands (`java -jar`). This method is mainly used when deploying the server or running in production environments.

3. **Building with Docker**: You can build the server as a Docker image and run it as a Docker container. This method maintains consistency across environments and has advantages in deployment and scaling.

## 4.1. Running with IDE (Gradle and React Project Execution)

The Open DID project consists of a backend (Spring Boot-based) and frontend (React-based), which can be developed and executed in IntelliJ IDEA and VS Code respectively.

### 4.1.1. Running Backend (Spring Boot) in IntelliJ IDEA

IntelliJ IDEA is an IDE widely used for Java development and is well compatible with Gradle-based projects. Since the Open DID server uses Gradle, it can be easily executed in IntelliJ.

#### 1. Install IntelliJ IDEA

- [Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

#### 2. Open Project

- Select `File -> New -> Project from Existing Sources`  
- Select `source/did-verifier-server` directory  
- The `build.gradle` file is automatically recognized, and necessary dependencies are automatically downloaded

#### 3. Gradle Build

- In the `Gradle` tab, execute `Tasks -> build -> build`

#### 4. Run Server

- Execute `Tasks -> application -> bootRun`  
- When the console displays `"Started [ApplicationName] in [time] seconds"` message, it's running normally

> ⚠️ Runs with default `sample` profile. Runs for testing purposes without database  
> For details, refer to [6. Profile Configuration and Usage](#6-profile-configuration-and-usage)

#### 5. Database Installation

- Uses PostgreSQL (Docker installation recommended)  
- For detailed installation methods, refer to [2.2. PostgreSQL Installation](#22-postgresql-installation)

#### 6. Server Configuration

- Configuration file location: `src/main/resources/config`  
- Example: DB connection information, ports settings, etc.  
- For detailed configuration methods, refer to [5. Configuration Guide](#5-configuration-guide)

---

### 4.1.2. Running Frontend (React) in VS Code

The Verifier admin console is React-based and can be executed separately in VS Code. It's useful for frontend development or UI verification.

#### 1. Install VS Code

- [Download VS Code](https://code.visualstudio.com/)

#### 2. Open Project

- Open `source/did-verifier-admin` directory in VS Code

#### 3. Install Dependencies

```bash
npm install
```

#### 4. Run Development Server

```bash
npm run dev
```

- Default access URL: [http://localhost:8092](http://localhost:8092)

> 📌 **Note:**  
> The backend (Spring Boot server) must be running separately,  
> and the API server address in the frontend can be specified through the `vite.config.ts` file or configuration files.
   

<br/>

## 4.2. Running with Console Commands

This section guides you through running the Open DID server using console commands. It explains the process of building the project using Gradle and running the server using the generated JAR file.
- When building with Gradle, the frontend (Admin Console) is automatically built together and included as static resources.

### 4.2.1. Gradle Build Commands

- Build the source using gradlew.
  ```shell
    # Move to the source folder of the cloned repository
    cd source/did-verifier-server

    # Grant execution permission to Gradle Wrapper
    chmod 755 ./gradlew

    # Clean build the project (delete previous build files and build new)
    ./gradlew clean build
  ```
  > Note: If frontend build is not needed (e.g., testing backend only or already including frontend artifacts), you can skip frontend build by adding the following option: 
  > - `./gradlew clean build -DskipFrontendBuild=true`

- Move to the built folder and verify that the JAR file is generated.
    ```shell
      cd build/libs
      ls
    ```
- This command generates the `did-verifier-server-3.0.0.jar` file.

<br/>

## 4.3. Running with Docker

- For Docker image building, configuration, execution, and other processes, refer to [7. Building and Running with Docker](#7-building-and-running-with-docker) below.

<br/>

# 5. Configuration Guide

This chapter guides you through all configuration values included in the server's configuration files. Each configuration is an important element that controls the server's operation and environment, and proper configuration is necessary for stable server operation. Please refer to the item-by-item descriptions and examples to apply configurations suitable for each environment.

Settings with the 🔒 icon are generally fixed values or values that typically don't need modification.

## 5.1. application.yml

- Role: The application.yml file defines basic settings for Spring Boot applications. Through this file, you can specify various environment variables such as application name, database settings, profile settings, etc., which significantly impact the application's operation.

- Location: `src/main/resources/`

### 5.1.1. Spring Basic Configuration

Spring's basic configuration defines the application name and profiles to activate, playing an important role in setting up the server's operating environment.

- `spring.application.name`: 🔒
  - Specifies the application name.
  - Purpose: Mainly used to identify applications in log messages, monitoring tools, or Spring Cloud services
  - Example: `Verifier`

- `spring.profiles.active`:  
  - Defines the profile to activate.
  - Purpose: Selects either sample or development environment to load configurations appropriate for that environment. For detailed profile information, refer to [6. Profile Configuration and Usage](#6-profile-configuration-and-usage).
  - Supported profiles: sample, dev
  - Example: `sample`, `dev`

- `spring.profiles.group.dev`: 🔒
  - Defines individual profiles included in the `dev` profile group.
  - Purpose: Groups and manages settings to be used in development environments.
  - Profile file naming convention: Configuration files corresponding to each profile use the name exactly as defined in the group. For example, the auth profile is written as application-auth.yml, databases profile as application-databases.yml. File names must use exactly the names written under group.dev.

- `spring.profiles.group.sample`: 🔒
  - Defines individual profiles included in the `sample` profile group.
  - Purpose: Groups and manages settings to be used in development environments.
  - Profile file naming convention: Configuration files corresponding to each profile use the name exactly as defined in the group. For example, the auth profile is written as application-auth.yml, databases profile as application-databases.yml. File names must use exactly the names written under group.dev.

<br/>

### 5.1.2. Jackson Basic Configuration

Jackson is the JSON serialization/deserialization library used by default in Spring Boot. Through Jackson configuration, you can adjust JSON data serialization methods or formats, improving performance and efficiency during data transmission.

- `spring.jackson.default-property-inclusion`: 🔒
  - Sets not to serialize when property values are null.
  - Example: non_null

- `spring.jackson.default-property-inclusion`: 🔒
  - Sets not to cause errors when serializing empty objects.
  - Example: false

<br/>

### 5.1.3. Server Configuration

Server configuration defines the port number where the application will receive requests.

- `server.port`:  
  - The port number where the application will run. The default port for Verifier server is 8092.
  - Value: 8092

<br/>

### 5.1.4. TAS Configuration

The Verifier service communicates with the TAS server. Set the address value of the TAS server you've built directly.

- `tas.url`:  
  - URL of the TAS (Trust Anchor Service) service. Can be used for authentication or trust verification.
  - The server adds `/tas` to this address.
  - Example: `http://localhost:8090`

<br/>

## 5.3. database.yml

- Role: Defines how the server will manage and operate databases, from database connection information to Liquibase migration settings and JPA configuration

- Location: `src/main/resources/`
  
### 5.3.1. Spring Liquibase Configuration

Liquibase is a tool for managing database migrations, helping track database schema changes and automatically apply them. This maintains database consistency across development and production environments.

- `spring.liquibase.change-log`: 🔒
  - Specifies the location of the database change log file. This is the location of the log file that Liquibase uses to track and apply database schema changes.
  - Example: `classpath:/db/changelog/master.xml`

- `spring.liquibase.enabled`: 🔒
  - Sets whether to enable Liquibase. When set to true, Liquibase runs when the application starts to perform database migration. The `sample` profile should be set to false since it doesn't integrate with databases.
  - Example: `true` [dev], `false` [sample]

- `spring.liquibase.fall-on-error`: 🔒
  - Controls behavior when errors occur during Liquibase database migration. Set only in `sample` profile.
  - Example: `false` [sample]

<br/>

### 5.3.2. Datasource Configuration

Datasource configuration defines basic information for the application to connect to the database. This includes information such as database driver, URL, username, and password.

- `spring.datasource.driver-class-name`: 🔒
  - Specifies the database driver class to use. Specifies the JDBC driver for connecting to the database.
  - Example: `org.postgresql.Driver`

- `spring.datasource.url`:  
  - Database connection URL. Specifies the location and name of the database the application will connect to.
  - Example: `jdbc:postgresql://localhost:5432/verifier_db`

- `spring.datasource.username`:  
  - Database access username.
  - Example: `verifier`

- `spring.datasource.password`:  
  - Database access password.
  - Example: `verifierpassword`

<br/>

### 5.3.3. JPA Configuration

JPA configuration controls how the application interacts with the database and significantly impacts performance and readability.

- `spring.jpa.open-in-view`: 🔒
  - Sets whether to use the OSIV (Open Session In View) pattern. When set to true, maintains database connection for the entire HTTP request.
  - Example: `true`

- `spring.jpa.show-sql`: 🔒
  - Sets whether to log SQL queries. When set to true, outputs executed SQL queries to logs. Useful for debugging during development.
  - Example: `true`

- `spring.jpa.hibernate.ddl-auto`: 🔒
  - Sets Hibernate's DDL automatic generation mode. Specifies database schema automatic generation strategy. When set to 'none', disables automatic generation.
  - Example: `none`

- `spring.jpa.hibernate.naming.physical-strategy`: 🔒
  - Sets database object naming strategy. Specifies the strategy for converting entity class names to database table names.
  - Example: `org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy`

- `spring.jpa.properties.hibernate.format_sql`: 🔒
  - Sets whether to format SQL. When set to false, disables formatting of SQL queries output to logs.
  - Example: `false`

<br/>

## 5.4. application-logging.yml

- Role: Sets log groups and log levels. Through this configuration file, you can define log groups for specific packages or modules and specify log levels individually for each group.

- Location: `src/main/resources/`
  
### 5.4.1. Logging Configuration

- Log Groups: Under logging.group, you can group and manage desired packages. For example, include the org.omnione.did.base.util package in the util group, and define other packages as their respective groups.

- Log Levels: Through logging.level settings, you can specify log levels for each group. You can set various log levels such as debug, info, warn, error to output logs at desired levels.

- `logging.level`:
  - Sets log levels.
  - By setting the level to debug, you can see all log messages at DEBUG level and above (INFO, WARN, ERROR, FATAL) for specified packages.

Full example:

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
  - Defines the URL path to access Swagger UI.
  - Example: `/swagger-ui.html`

- `springdoc.swagger-ui.groups-order`: 🔒
  - Specifies the order of displaying API groups in Swagger UI.
  - Example: `ASC`

- `springdoc.swagger-ui.operations-sorter`: 🔒
  - Sorts API endpoints by HTTP method in Swagger UI.
  - Example: `method`

- `springdoc.swagger-ui.disable-swagger-default-url`: 🔒
  - Disables the default Swagger URL.
  - Example: `true`

- `springdoc.swagger-ui.display-request-duration`: 🔒
  - Sets whether to display request time in Swagger UI.
  - Example: `true`

- `springdoc.api-docs.path`: 🔒
  - Defines the path where API documentation is served.
  - Example: `/api-docs`

- `springdoc.show-actuator`: 🔒
  - Sets whether to display Actuator endpoints in API documentation.
  - Example: `true`

- `springdoc.default-consumes-media-type`: 🔒
  - Sets the default media type for request bodies in API documentation.
  - Example: `application/json`

- `springdoc.default-produces-media-type`: 🔒
  - Sets the default media type for response bodies in API documentation.
  - Example: `application/json`

<br/>

## 5.6. application-wallet.yml

- Role: Configures wallet file information used by the server.

- Location: `src/main/resources/`

- `wallet.file-path`:  
  - Specifies the path to the wallet file. Specifies the location of the file that stores the file wallet. This file may contain important information such as private keys. *Must be entered as an absolute path*
  - Example: `/path/to/your/verifier.wallet`

- `wallet.password`:  
  - Password used for wallet access. Password used when accessing the wallet file. This is information that requires high security.
  - Example: `your_secure_wallet_password`

## 5.7. blockchain.properties

- Role: Configures blockchain server information for integration with the Verifier server. When you install the Hyperledger Besu network according to '5.3. Step 3: Blockchain Installation' in [Open DID Installation Guide], private keys, certificates, and server connection information configuration files are automatically generated. In blockchain.properties, you set the paths where these files are located and the network name entered during Hyperledger Besu installation.

- Location: `src/main/resources/properties`

### 5.7.1. Blockchain Integration Configuration

#### EVM Network Configuration

- `evm.network.url`:
  - EVM Network address. Use this fixed value when running Besu on the same local as the client. (Default Port: 8545)
  - Example: http://localhost:8545

- `evm.chainId`:
  - Chain ID identifier. Currently uses a fixed value of 1337. (Default Value: 1337)
  - Example: 1337

- `evm.gas.limit`:
  - Maximum gas limit allowed for Hyperledger Besu EVM transactions. Currently uses a fixed value as Free Gas. (Default Value: 100000000)
  - Example: 100000000

- `evm.gas.price`:
  - Gas price per unit. Currently uses a fixed value of 0 as Free Gas. (Default Value: 0)
  - Example: 0

- `evm.connection.timeout`: 
  - Network connection timeout value (milliseconds). Currently uses the recommended fixed value of 10000. (Default Value: 10000)
  - Example: 10000

#### EVM Contract Configuration

- `evm.connection.address`: 
  - Address value of the OpenDID Contract returned when deploying Smart Contract with Hardhat. For detailed guide, refer to [DID Besu Contract].
  - Example: 0xa0E49611FB410c00f425E83A4240e1681c51DDf4

- `evm.connection.privateKey`: 
  - k1 key used for API access control. Enter the key string defined in accounts inside hardhat.config.js (remove the 0x string at the beginning) to enable API calls with Owner privileges (Default setting). For detailed guide, refer to [DID Besu Contract].
  - Example: 0x8f2a55949038a9610f50fb23b5883af3b4ecb3c3bb792cbcefbd1542c692be63
<br/>


## 5.8. application-verifier.yml (Status List)

- Role: Configures the credential status check that uses an IETF Token Status List (SD-JWT VC and mso_mdoc credentials submitted through OID4VP).

- Location: External configuration file. The `dev` profile group includes the `verifier` profile, so create `application-verifier.yml` in the configuration directory only when you need to change the defaults below.

- `verifier.status-list.fail-on-fetch-error`:
  - Whether to reject the submission when the status list cannot be fetched. If `false`, the status check is skipped and verification continues. (Default Value: true)
  - Example: true

- `verifier.status-list.max-cache-ttl-seconds`, `verifier.status-list.min-cache-ttl-seconds`:
  - Upper and lower bounds of the cache time for a fetched status list token. The `ttl` claim of the token is used within these bounds. (Default Values: 86400, 60)
  - Example: 86400, 60

- `verifier.status-list.max-decompressed-bytes`:
  - Maximum size of a decompressed status list. Larger lists are rejected. (Default Value: 1048576)
  - Example: 1048576

- `verifier.status-list.allowed-private-hosts`:
  - The status list URI must use `http` or `https`, and a host that resolves to a loopback, private, or link-local address is rejected. List hosts on a closed network or a local development server here (exact host match). (Default Value: empty)
  - Example: `[192.168.0.10, localhost]`

## 5.9. application-lss.yml

- Role: Configures integration with the LSS (Ledger Service Server). When the `lss` profile is active, the Verifier reads DID Documents from LSS instead of the blockchain.

- Location: `src/main/resources/config`

- `lss.url`:
  - LSS server address.
  - Example: `http://127.0.0.1:8098`

- LSS API Key:
  - The Verifier sends the LSS API key in the `X-API-Key` header. Register the key issued by LSS as `API_KEY_LSS` in Server Configuration of the Admin Console. If it is not registered, LSS calls fail with `SSRVVRF21501`.

## 5.10. OID4VP Configuration

- Role: The OID4VP configuration is stored in the `oid4vp_config` table, not in a yml file, and is managed in the Admin Console. The scope used by an `OID4VP` policy is mapped to a DCQL query in DCQL Scope Mapping of the Admin Console.

- `crypto.vpTokenEncryptionKey`:
  - AES-256 key (Base64, 32 bytes) that encrypts the stored VP Token and the per-session response decryption key. The initial data contains a sample key, so **replace it before operation**. Example of generating a key: `openssl rand -base64 32`
  - Sessions created before the key is replaced cannot be decrypted (`SSRVVRF01310`). Replace it when no verification is in progress.
  - The Admin API `GET /verifier/admin/v1/oid4vp/config` returns the configuration including this key in plaintext (the Admin Console screen masks it). Restrict access to the Admin Console.

- Fixed values:
  - `responseMode` is fixed to `direct_post.jwt`, and response encryption is fixed to `alg` ECDH-ES and `enc` A256GCM. Saving other values fails (`SSRVVRF01315`, `SSRVVRF01313`).


# 6. Profile Configuration and Usage

## 6.1. Profile Overview (`sample`, `dev`, `lss`)

The Verifier server supports two profiles, `dev` and `sample`, to run in various environments.

Each profile is designed to apply configurations appropriate for the corresponding environment. By default, the Verifier server is set to the `sample` profile, which is designed to run the server independently without integration with external services such as databases or blockchains. The `sample` profile is suitable for API call testing, allowing developers to quickly verify the basic operation of the application. This profile returns fixed response data for all API calls, making it useful in initial development environments.

Sample API calls are written as JUnit tests, so you can refer to them when writing tests.

In contrast, the `dev` profile is designed to perform actual operations. Using this profile enables testing and verification with real data. When the `dev` profile is activated, it integrates with actual external services such as databases and blockchains, allowing you to test application behavior in real environments.

### 6.1.1. `sample` Profile
The `sample` profile is designed to run the server independently without integration with external services (DB, blockchain, etc.). This profile is suitable for API call testing and allows developers to quickly verify the basic operation of the application. Since it returns fixed response data for all API calls, it's useful for initial development stages or functional testing. Because no integration with external systems is required, it provides an environment where you can run and test the server standalone.
> Note: When using the sample profile, the Admin Console will not work.

### 6.1.2. `dev` Profile

The `dev` profile includes configurations suitable for development environments and is used on development servers. To use this profile, configuration for development environment databases and blockchain nodes is required.

### 6.1.3. `lss` Profile

The `lss` profile is used together with `dev` (for example, `dev,lss`). The Verifier reads DID Documents from LSS instead of the blockchain, so blockchain configuration is not required. Set `lss.url` (5.9) and register `API_KEY_LSS` in the Admin Console.

## 6.2. Profile Configuration Methods

This section explains how to change profiles for each running method.

### 6.2.1. When Running Server with IDE

- **Configuration File Selection:** Select the `application.yml` file in the `src/main/resources` path.
- **Profile Specification:** Add the `--spring.profiles.active={profile}` option in the IDE's run settings (Run/Debug Configurations) to activate the desired profile.
- **Configuration Application:** The corresponding configuration file is applied according to the activated profile.

### 6.2.2. When Running Server with Console Commands

- **Configuration File Selection:** Prepare profile-specific configuration files in the same directory as the built JAR file or in the path where configuration files are located.
- **Profile Specification:** Add the `--spring.profiles.active={profile}` option to the server running command to activate the desired profile.
  
  ```bash
  java -jar build/libs/did-verifier-server-3.0.0.jar --spring.profiles.active={profile}
  ```

- **Configuration Application:** The corresponding configuration file is applied according to the activated profile.

### 6.2.3. When Running Server with Docker

- **Configuration File Selection:** When creating a Docker image, specify the configuration file path in the Dockerfile or mount external configuration files to the Docker container.
- **Profile Specification:** Set the profile by configuring the `SPRING_PROFILES_ACTIVE` environment variable in the Docker Compose file or Docker execution command.
  
  ```yaml
  environment:
    - SPRING_PROFILES_ACTIVE={profile}
  ```

- **Configuration Application:** Configuration is applied according to the specified profile when running the Docker container.

You can flexibly change profile-specific configurations according to each method and easily apply configurations suitable for your project environment.

# 7. Running After Building with Docker

## 7.1. Docker Image Build Method (Based on `Dockerfile`)

### 7.1.1. Build Docker image
Build the Docker image with the following command:

```bash
cd {source_directory}
docker build -t did-verifier-server -f did-verifier-server/Dockerfile .
```

<br/>

## 7.2. Running with Docker Compose

### 7.2.1. Preparing Directories and Configuration Files

#### 1. Create docker-compose directory and config directory
```bash
mkdir -p {docker_compose_directory}/config
```

#### 2. Copy configuration files (yml) to config directory
```bash
cp {application_yml_directory}/* {docker_compose_directory}/config/
cp {blockchain_properties_path} {docker_compose_directory}/config/
```

#### 3. Modify blockchain.properties file
```yml
evm.network.url=http://host.docker.internal:8545
... (omitted)
```

> **host.docker.internal** is a special address that points to the host machine from within a Docker container.  
> Since localhost inside a container refers to the container itself, you must use host.docker.internal to access services (PostgreSQL, blockchain) running on the host.

#### 4. Modify application-database.yml file
```yml
spring:
 ... (omitted)
 datasource:
   driver-class-name: org.postgresql.Driver
   url: jdbc:postgresql://host.docker.internal:5430/verifier
   username: omn
   password: omn
 ... (omitted)
```

### 7.2.2. Create `docker-compose.yml` file
You can easily manage multiple containers using the `docker-compose.yml` file.

```yml
version: '3'
services:
 app:
   image: did-verifier-server
   ports:
     - "8092:8092"
   volumes:
     - {config_directory}:/app/config
   environment:
     - SPRING_PROFILES_ACTIVE=dev
   extra_hosts:
     - "host.docker.internal:host-gateway"
```

> - In the example above, the `config_directory` is mounted to `/app/config` inside the container to share configuration files.
>   - Configuration files located in `config_directory` take priority over default configuration files.
>   - For detailed configuration instructions, please refer to [5. Configuration Guide](#5-configuration-guide).


### 7.2.3. Run Container
```bash
cd {docker_compose_directory}
docker-compose up -d
```

<br/>

# 8. Installing Docker PostgreSQL

This section explains how to install PostgreSQL using Docker. Through this method, you can easily install PostgreSQL and integrate it with the server for use.

## 8.1. PostgreSQL Installation using Docker Compose

The following shows how to install PostgreSQL using Docker Compose.

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

This Docker Compose file installs PostgreSQL version 16.4 and makes the following configurations:

- **container_name**: Names the container `postgre-verifier`.
- **volumes**: Mounts the `postgres_data_verifier` volume to PostgreSQL's data directory (`/var/lib/postgresql/data`). This ensures data is permanently preserved.
- **ports**: Maps the host's port 5432 to the container's port 5432.
- **environment**: Sets PostgreSQL's username, password, and database name. Here, `${USER}` and `${PW}` can be set as environment variables.

## 8.2. PostgreSQL Container Execution

To run the PostgreSQL container using the above Docker Compose file, execute the following command in the terminal:

```bash
docker-compose up -d
```

This command runs the PostgreSQL container in the background. The PostgreSQL server runs according to the configured environment variables, and the database is prepared. You can then proceed with integration settings to use this database in your application.

[Open DID Installation Guide]: https://github.com/OmniOneID/did-release/blob/main/release-V2.0.0.0/OpenDID_Installation_Guide-Orchestrator-V2.0.0.0.md
[Open DID Admin Console Guide]: ../admin/OpenDID_VerifierAdmin_Operation_Guide.md
[DID Besu Contract]: https://github.com/OmniOneID/did-besu-contract