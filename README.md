# Fingertip Backend

Spring Boot 3.x 기반의 "장인과하루" 백엔드 API 서버입니다.

기획서 v4 기준 이 서버는 앱이 직접 호출하는 유일한 외부 API입니다. AI 해설, 번역, 후기 요약이 필요할 때만 내부 FastAPI 서버(`../backend_python`)를 호출하며, 프론트엔드가 FastAPI를 직접 호출하지 않습니다.

## 프로젝트 개요

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.3.0
- **빌드 도구**: Gradle
- **데이터베이스**: PostgreSQL
- **캐시**: Redis
- **클라우드 스토리지**: AWS S3
- **결제**: 토스페이먼츠
- **인증**: JWT + 소셜 로그인 (카카오, 구글, 애플)

## 프로젝트 구조

```
com/janginharou/
├── domain/
│   ├── user/              # 사용자 관리
│   ├── artisan/           # 장인 관리
│   ├── experience/        # 체험 프로그램
│   ├── reservation/       # 예약 관리
│   ├── review/            # 후기
│   ├── cardnews/          # 카드뉴스
│   └── notification/      # 알림
├── global/
│   ├── config/            # 설정 (Security, JWT, S3, Redis, Async)
│   ├── exception/         # 커스텀 예외 및 예외 핸들러
│   └── common/            # 공용 클래스 (BaseEntity, ApiResponse)
└── FingertipBackendApplication.java
```

## 주요 도메인

### 1. User (사용자)
- 이메일, 카카오, 구글, 애플 소셜 로그인 지원
- 프로필 관리

### 2. Artisan (장인)
- 무형유산 종목 등록
- 인증 상태 관리 (PENDING / APPROVED / REJECTED)
- 인증서 이미지 S3 업로드

### 3. Experience (체험 프로그램)
- 장인이 제공하는 체험 프로그램
- 가격, 일정, 최대 인원, 난이도, 언어 지원

### 4. Reservation (예약)
- 상태머신: PENDING → APPROVED → PAID → CONFIRMED (또는 REJECTED / CANCELLED)
- 토스페이먼츠 결제 연동

### 5. Review (후기)
- 별점 (1~5)
- 사진, 텍스트, '새로 알게 된 것' 항목
- 관리자 승인 대기

### 6. CardNews (한물결 카드뉴스)
- K-콘텐츠 유형별 분류
- 체험 프로그램 연결
- 개인화 태그
- 조회수 추적 (Redis 캐싱 예정)

### 7. Notification (알림)
- 예약 확정/거절 알림
- 결제 리마인더
- 비동기 발송 (@Async)

### 8. FastAPI AI 연동
- 문화 해설, 관련 체험 추천, 번역, 후기 요약을 내부 HTTP로 요청
- AI 응답 실패 시 앱 응답 정책과 timeout/fallback 처리
- 공공데이터 적재와 벡터 검색 구현은 FastAPI 서버 소유

## 환경 설정

### 로컬 개발 환경

#### 0. JDK 17 설치
```bash
# macOS
brew install openjdk@17

# JAVA_HOME 설정 (Java 25 등 상위 버전 사용 시 빌드 실패하므로 반드시 JDK 17 사용)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# 빌드 실행
./gradlew build
```

#### 1. Docker 설정
```bash
# 프로젝트 루트로 이동
cd backend

# PostgreSQL(janginharou DB) + Redis 실행
docker compose up -d

# 상태 확인
docker compose ps
```

#### 2. 프로젝트 빌드 및 실행
```bash
# 빌드
./gradlew clean build

# 로컬 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 환경 변수 설정 (로컬)

`application-local.yml`에 다음 환경이 자동 설정됩니다:
- 데이터베이스: `localhost:5432/janginharou` (계정: postgres/postgres)
- Redis: `localhost:6379`
- JWT Secret: 256비트 이상의 보안 키

### 프로덕션 환경

환경 변수로 설정 필요:
- `DB_URL`: PostgreSQL 연결 URL
- `DB_USER`: 데이터베이스 사용자
- `DB_PASSWORD`: 데이터베이스 비밀번호
- `REDIS_HOST`: Redis 호스트
- `REDIS_PORT`: Redis 포트
- `REDIS_PASSWORD`: Redis 비밀번호
- `JWT_SECRET`: JWT 서명 키
- `AWS_ACCESS_KEY_ID`: AWS 액세스 키
- `AWS_SECRET_ACCESS_KEY`: AWS 시크릿 키
- `AWS_S3_BUCKET`: S3 버킷명

## API 문서

서버 실행 후 Swagger UI 접속:
```
http://localhost:8080/api/swagger-ui.html
```

## 주요 기능

### 예약 상태머신
```
PENDING (사용자 예약 신청)
   ↓
APPROVED (장인이 승인) → REJECTED (거절)
   ↓
PAID (결제 완료)
   ↓
CONFIRMED (최종 확정)
```

## TODO 항목

각 Service 클래스에 `// TODO` 주석으로 구현 필요 사항 표시:
- 소셜 로그인 토큰 검증
- S3 이미지 업로드
- 토스페이먼츠 웹훅 처리
- Firebase Cloud Messaging 푸시 알림
- Redis 캐싱 활용
- 권한 검증 (AOP 또는 @PreAuthorize)

## 의존성

### Spring Framework
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-data-redis
- spring-boot-starter-validation

### Database & ORM
- postgresql
- flyway-core (마이그레이션)

### Authentication
- jjwt (JWT)

### Cloud & Storage
- software.amazon.awssdk:s3

### Documentation
- springdoc-openapi (Swagger)

### Development
- lombok
- mapstruct (선택사항, DTO 매핑)

## 로컬 실행

```bash
# 1. 프로젝트 디렉토리로 이동
cd backend

# 2. Gradle 빌드
./gradlew clean build

# 3. 로컬 환경으로 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 또는 IDE에서 FingertipBackendApplication.java 실행
```

서버는 `http://localhost:8080/api`에서 시작됩니다.

## 다음 단계

1. `TODO.md` 기준으로 2026-06-28 MVP API 범위를 고정
2. 데이터베이스 스키마와 Flyway 마이그레이션 작성
3. 각 Service 클래스의 예약/결제/권한 TODO 구현
4. FastAPI AI 서버 내부 HTTP 클라이언트와 장애 처리 구현
5. 단위 테스트 및 예약-결제-해설 통합 테스트 작성
6. 보안 및 권한 관리 강화

## 참고

- JWT 설정: `global/config/JwtTokenProvider.java`
- Spring Security 설정: `global/config/SecurityConfig.java`
- S3 설정: `global/config/S3Config.java`
- 비동기 설정: `global/config/AsyncConfig.java`
- 전역 예외 처리: `global/exception/GlobalExceptionHandler.java`
