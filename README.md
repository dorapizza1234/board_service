# 📋 Board Service

> MSA 기반 게시판 마이크로서비스 — 공지사항 · FAQ · 1:1 문의를 담당합니다.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![Oracle](https://img.shields.io/badge/Oracle%20XE-DB-F80000?style=flat-square&logo=oracle&logoColor=white)](https://www.oracle.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)

---

## 목차

- [아키텍처 개요](#아키텍처-개요)
- [기술 스택](#기술-스택)
- [주요 기능](#주요-기능)
- [프로젝트 구조](#프로젝트-구조)
- [API 명세](#api-명세)
- [보안 구조](#보안-구조)
- [데이터 모델](#데이터-모델)
- [실행 방법](#실행-방법)

---

## 아키텍처 개요

이 서비스는 **MSA(마이크로서비스 아키텍처)** 환경의 일부로 동작합니다.
클라이언트의 모든 요청은 **API Gateway**를 통해 라우팅되며, **Eureka Server**에 자동 등록됩니다.

```
┌──────────────┐
│   클라이언트   │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│   API Gateway    │  ← 라우팅 / 인증 전처리
└──────┬───────────┘
       │  /board-service/**
       ▼
┌──────────────────┐     ┌──────────────────┐
│  Board Service   │────▶│   Eureka Server  │
│   (Port: 8002)   │     │  (localhost:8761) │
└──────┬───────────┘     └──────────────────┘
       │
       ▼
┌──────────────────┐
│    Oracle DB     │
└──────────────────┘
```

> API Gateway가 JWT 토큰 검증 및 라우팅을 담당하고,
> Board Service는 비즈니스 로직과 데이터 처리에 집중합니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.11 |
| Cloud | Spring Cloud 2025.0.1 (Eureka Client) |
| ORM | Spring Data JPA + Hibernate |
| Database | Oracle XE |
| Connection Pool | HikariCP |
| Security | Spring Security + JWT (JJWT 0.13.0) |
| View | Thymeleaf + Spring Security Extras |
| Build | Gradle (Groovy DSL) |
| Port | **8002** |

---

## 주요 기능

### 공지사항 (Notice)
- 공지사항 / FAQ 목록 조회 (페이지네이션)
- 공지사항 상세 조회 (조회수 자동 증가)
- 관리자 전용: 작성 / 수정 / 소프트 삭제
- 중요도(0~9) 설정 및 게시 상태 관리

### FAQ
- 공지사항과 동일한 엔티티를 `noticeType = 'FAQ'` 로 구분
- 별도 FAQ 목록 조회 지원

### 1:1 문의 (Inquiry)
- 로그인 회원 문의 작성 / 조회 (본인 문의만 열람 가능)
- 문의 상태 관리: `대기` → `답변완료`
- 관리자 전용: 전체 문의 목록 조회 / 답변 작성
- 대시보드용 JSON API 제공

---

## 프로젝트 구조

```
src/main/java/com/spring/app/
├── BoardServiceApplication.java        # 진입점
│
├── auth/domain/
│   └── CustomUserDetails.java          # UserDetails 구현체
│
├── config/
│   └── SecurityConfig.java             # Spring Security 설정
│
├── security/
│   ├── JwtToken.java                   # JWT 토큰 객체
│   ├── JwtTokenProvider.java           # 토큰 생성/검증
│   └── JwtAuthenticationFilter.java    # 요청 필터 (헤더/쿠키 추출)
│
├── entity/
│   ├── Notice.java                     # 공지사항/FAQ 엔티티
│   └── Inquiry.java                    # 1:1 문의 엔티티
│
├── member/domain/
│   └── MemberDTO.java                  # JWT에서 파싱된 회원 정보
│
├── notice/
│   ├── controller/NoticeController.java
│   ├── domain/NoticeDTO.java
│   ├── repository/NoticeRepository.java
│   └── service/
│       ├── NoticeService.java          # 인터페이스
│       └── NoticeService_imple.java    # 구현체
│
└── inquiry/
    ├── controller/InquiryController.java
    ├── domain/InquiryDTO.java
    ├── repository/InquiryRepository.java
    └── service/
        ├── InquiryService.java         # 인터페이스
        └── InquiryService_imple.java   # 구현체

src/main/resources/
├── application.yml
└── templates/
    ├── index.html
    ├── fragment/          # header, footer
    ├── layout/            # layout_main
    ├── notice/            # list, view, write, edit, admin_list
    └── inquiry/           # list, view, write, admin_list
```

---

## API 명세

### 공지사항 API

| Method | Path | 권한 | 설명 |
|--------|------|------|------|
| `GET` | `/notice/list` | 공개 | 공지사항 + FAQ 목록 (페이징) |
| `GET` | `/notice/view` | 공개 | 공지사항 상세 (조회수 증가) |
| `GET` | `/notice/write` | ADMIN | 작성 폼 |
| `POST` | `/notice/write` | ADMIN | 공지사항 저장 |
| `POST` | `/notice/edit` | ADMIN | 공지사항 수정 |
| `DELETE` | `/notice/delete` | ADMIN | 소프트 삭제 |
| `GET` | `/notice/admin/list` | ADMIN | 관리자용 전체 목록 |

### 1:1 문의 API

| Method | Path | 권한 | 설명 |
|--------|------|------|------|
| `GET` | `/inquiry/list` | 공개 | 내 문의 목록 |
| `GET` | `/inquiry/write` | 공개 | 문의 작성 폼 |
| `POST` | `/inquiry/write` | USER / ADMIN | 문의 저장 |
| `GET` | `/inquiry/view` | USER / ADMIN | 문의 상세 (본인 또는 관리자) |
| `GET` | `/inquiry/admin/list` | ADMIN | 관리자 문의 목록 (상태 필터) |
| `GET` | `/inquiry/admin/api` | 공개 | 대시보드용 JSON |
| `POST` | `/inquiry/admin/answer` | ADMIN | 관리자 답변 저장 |

---

## 보안 구조

```
HTTP 요청
    │
    ▼
JwtAuthenticationFilter
    ├── Authorization 헤더에서 "Bearer {token}" 추출  (REST)
    └── HttpOnly 쿠키 "accessToken" 에서 추출         (브라우저)
            │
            ▼
    JwtTokenProvider.validateToken()
            │
            ▼
    SecurityContextHolder 에 Authentication 등록
            │
            ▼
    SecurityConfig 경로 권한 검사
    ├── 공개: /notice/list, /notice/view, /inquiry/list, /inquiry/admin/api
    ├── ADMIN: /admin/**, /notice/write, /notice/edit, ...
    └── USER or ADMIN: /inquiry/view, /inquiry/write
```

**JWT 설정**

| 항목 | 값 |
|------|-----|
| 알고리즘 | HMAC-SHA256 (JJWT 0.13.0) |
| Access Token 유효기간 | 60분 |
| Refresh Token 유효기간 | 7일 |
| 세션 전략 | STATELESS (서버 세션 없음) |
| CSRF | 비활성화 |

> 이 서비스는 자체 회원 DB를 보유하지 않습니다.
> 회원 인증은 별도 **Auth / Member Service**에서 처리하며,
> Board Service는 **JWT 토큰 검증만** 수행합니다.

---

## 데이터 모델

### Notice (공지사항 / FAQ)

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `notice_id` | Long (PK) | Oracle 시퀀스 자동 생성 |
| `admin_email` | String(100) | 작성 관리자 이메일 |
| `title` | String(400) | 제목 |
| `content` | CLOB | 본문 |
| `notice_type` | String | `NOTICE` or `FAQ` |
| `status` | String | `PUBLISHED` (기본값) |
| `is_deleted` | String | 소프트 삭제 `Y` / `N` |
| `view_count` | Integer | 조회수 (DB 레벨 증가) |
| `importance` | Integer | 중요도 (0~9) |
| `created_at` | LocalDate | 작성일 (auto) |

### Inquiry (1:1 문의) — 테이블명: `INQUIRIES`

| 컬럼 | 타입 | 설명 |
|------|------|------|
| `inquiry_id` | Long (PK) | `SEQ_INQUIRY_ID` 시퀀스 |
| `member_email` | String(100) | 문의자 이메일 |
| `title` | String(200) | 제목 |
| `content` | CLOB | 문의 내용 |
| `created_at` | LocalDate | 작성일 (auto) |
| `inquiry_status` | String | `대기` → `답변완료` |
| `admin_answer` | CLOB | 관리자 답변 |
| `answered_at` | LocalDate | 답변 일시 |

---

## 실행 방법

### 사전 요구사항

- Java 17
- Oracle XE (또는 접근 가능한 Oracle DB)
- Eureka Server 실행 중 (`localhost:8761`)
- API Gateway 실행 중

### 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

또는

```bash
java -jar build/libs/board_service-*.jar
```

서비스는 `http://localhost:8002` 에서 실행되며,
API Gateway를 통해 `/board-service/**` 경로로 접근합니다.

### application.yml 주요 설정

```yaml
server:
  port: 8002

spring:
  application:
    name: board-service   # Eureka 등록명
  datasource:
    url: jdbc:oracle:thin:@//[DB_HOST]:1521/xe
    hikari:
      minimum-idle: 5
      maximum-pool-size: 10

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

<div align="center">

**Board Service** is part of an MSA project built with Spring Cloud & API Gateway.

</div>
