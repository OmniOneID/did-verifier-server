Verifier Server
==

Verifier 서버 Repository에 오신 것을 환영합니다. <br>
이 Repository는 Verifier 서버의 소스 코드, 문서, 관련 리소스를 포함하고 있습니다.

## S/W 사양
| 구분              | 내용                                 |
|-------------------|--------------------------------------|
| OS                | macOS / Linux / Windows 10 이상       |
| Language          | Java 21 이상                          |
| IDE               | IntelliJ IDEA                         |
| Build System      | Gradle 7.0 이상                        |
| Compatibility     | JDK 21 이상                            |
| Docker            | Docker 및 Docker Compose 설치 필요     |
| 기타 요구사항      | 최소 2GB RAM, 10GB 디스크 공간 이상     |

## 폴더 구조
프로젝트 디렉터리 내 주요 폴더와 문서에 대한 개요:

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
│   └── admin
│       ├── OpenDID_VerifierAdmin_Operation_Guide.md
│       └── OpenDID_VerifierAdmin_Operation_Guide_ko.md
│   └── api
│       ├── Verifier_API.md  
│       └── Verifier_API_ko.md
│   └── errorCode
│       ├── Verifier_ErrorCode.md
│       └── Verifier_ErrorCode_ko.md
│   └── db
│       ├── OpenDID_TableDefinition_Verifier.md
│       └── OpenDID_TableDefinition_Verifier_ko.md
│   └── installation
│       └── OpenDID_VerifierServer_InstallationAndOperation_Guide.md
│       └── OpenDID_VerifierServer_InstallationAndOperation_Guide_ko.md
└── source
    └── did-verifier-admin
        ├── frontend
    └── did-verifier-server
```

<br/>

각 폴더와 파일에 대한 설명은 다음과 같습니다:

| 이름                         | 설명                                     |
| ---------------------------- | ---------------------------------------- |
| CHANGELOG.md                 | 프로젝트의 버전별 변경 사항              |
| CODE_OF_CONDUCT.md           | 기여자 행동 강령                         |
| CONTRIBUTING.md              | 기여 지침과 절차                         |
| LICENSE                      | 라이선스                                 |
| dependencies-license.md      | 프로젝트 의존 라이브러리의 라이선스 정보 |
| MAINTAINERS.md               | 프로젝트 유지 관리자 지침                |
| RELEASE-PROCESS.md           | 새 버전 릴리스 절차                      |
| SECURITY.md                  | 보안 정책 및 취약성 보고 방법            |
| docs                         | 문서화                                   |
| ┖ admin                      | Admin Console 가이드 문서                |
| ┖ api                        | API 가이드 문서                          |
| ┖ errorCode                  | 오류 코드 및 문제 해결 가이드            |
| ┖ installation               | 설치 및 설정 지침                        |
| ┖ db                         | 데이터베이스 ERD, 테이블 명세서          |
| source                       | 서버 및 관리자 콘솔 소스 코드 프로젝트   |
| ┖ did-verifier-server        | Verifier 서버 소스 코드                  |
| ┖ did-verifier-admin         | Verifier Admin 소스 코드                 |
| &nbsp;&nbsp;&nbsp;┖ frontend | Verifier Admin 프론트엔드 소스 코드      |

<br/>

## 설치 및 운영 가이드

Verifier 서버의 설치 및 구성에 대한 자세한 지침은 아래 가이드를 참조하십시오:
- [OpenDID Verifier 서버 설치 가이드](docs/installation/OpenDID_VerifierServer_Installation_Guide.md)  

Verifier Admin Console의 운영에 대한 자세한 지침은 아래 가이드를 참조하십시오:
- [OpenDID Verifier Admin Console 운영 가이드](docs/admin/OpenDID_VerifierAdmin_Operation_Guide_ko.md)  

## API 참고 문서

- **Verifier API**: Verifier 서버의 API 엔드포인트 및 사용법에 대한 자세한 참고 자료입니다.
  - [Verifier API 참고 자료](docs/api/Verifier_API_ko.md)

## Change Log

Change Log에는 버전별 변경 사항과 업데이트가 자세히 기록되어 있습니다. 다음에서 확인할 수 있습니다:
- [Change Log](./CHANGELOG.md)  

## OpenDID 시연 영상

OpenDID 시스템의 시연 영상을 보려면 [데모 Repository](https://github.com/OmniOneID/did-demo-server)를 방문하십시오. <br>

이 영상에서는 사용자 등록, VC 발급, VP 제출 프로세스 등 주요 기능을 시연합니다.

## 기여

기여 절차와 행동 강령에 대한 자세한 내용은 [CONTRIBUTING.md](CONTRIBUTING.md)와 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)를 참조해 주십시오.

## 라이선스
[Apache 2.0](LICENSE)
