# 🚀 커뮤니티 서비스

> **Spring Boot와 Kubernetes 기반의 백엔드 커뮤니티 서비스 API Engine**

<br />

## 📌 목차
- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시작하기 (Getting Started)](#-시작하기-getting-started)
- [API 명세서](#-api-명세서)
- [개발 인원](#-개발-인원)

<br />

## 📝 프로젝트 소개
* **개발 기간**: 2026.05 ~ 진행 중
* **서비스 설명**: 
  안정적인 유저 관리와 보안 강화를 위해 JWT 기반의 인증/인가 시스템을 구축한 커뮤니티 백엔드 서비스입니다. GitHub Actions, Helm, Argo CD를 활용한 GitOps CI/CD 파이프라인 구축 및 Kubernetes, Prometheus, Grafana 기반의 모니터링 환경에서 확장성 높고 안정적인 아키텍처를 목표로 개발되었습니다.

<br />

## ⚙️ 주요 기능
- **유저 CRUD**: 회원가입, 프로필 조회, 회원 정보 수정, 탈퇴 기능 구현
- **인증 및 인가**: JWT(JSON Web Token)를 이용한 안전한 토큰 기반 로그인/인증 처리
- **게시글 / 댓글 관리**: 커뮤니티 게시글 작성, 조회, 수정, 삭제 기능

<br />

## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=OpenJDK&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=Spring-Boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=MySQL&logoColor=white)

### DevOps & CI/CD
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=GitHub-Actions&logoColor=white)
![ArgoCD](https://img.shields.io/badge/Argo_CD-EF7B4D?style=flat-square&logo=Argo&logoColor=white)
![Helm](https://img.shields.io/badge/Helm-0F1689?style=flat-square&logo=Helm&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=Kubernetes&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=Docker&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS_RDS-527FFF?style=flat-square&logo=Amazon-RDS&logoColor=white)

### Monitoring & Observability
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=flat-square&logo=Prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat-square&logo=Grafana&logoColor=white)

### Environment & Tools
![Git](https://img.shields.io/badge/Git-F05032?style=flat-square&logo=Git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=GitHub&logoColor=white)

<br />

## 🏁 시작하기 (Getting Started)

프로젝트를 로컬 환경에서 실행하는 방법입니다.

### Prerequisites (사전 요구사항)
- Java 17 이상
- Gradle
- MySQL (또는 AWS RDS 연결 설정)
- Docker / Kubernetes / Helm / Argo CD (선택 사항)
- Prometheus & Grafana (모니터링 옵션)

### Installation & Execution (설치 및 실행)

1. Repository 클론
```bash
git clone [https://github.com/dhkiss01/community.git](https://github.com/dhkiss01/community.git)
cd community