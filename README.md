# Fingertip Backend

Spring Boot 3.x 기반의 "장인과하루" 백엔드 API 서버입니다.

## 프로젝트 개요

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.4
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

## 환경 설정

### 로컬 개발 환경

#### 1. PostgreSQL 설치
```bash
# macOS
brew install postgresql

# PostgreSQL 시작
brew services start postgresql

# 데이터베이스 생성
createdb fingertip_dev

# 사용자 생성 (기본: postgres / postgres)
psql -U postgres
CREATE USER postgres WITH PASSWORD 'postgres';
ALTER ROLE postgres SUPERUSER;
```

#### 2. Redis 설치
```bash
# macOS
brew install redis

# Redis 시작
brew services start redis
```

#### 3. 프로젝트 설정
```bash
# 프로젝트 루트로 이동
cd backend

# 빌드
./gradlew build

# 로컬 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 환경 변수 설정 (로컬)

`application-local.yml`에 다음 환경이 자동 설정됩니다:
- 데이터베이스: `localhost:5432/fingertip_dev`
- Redis: `localhost:6379`
- JWT Secret: `local-secret-key-change-in-production`

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

1. 데이터베이스 스키마 (Flyway 마이그레이션 파일) 작성
2. 각 Service 클래스의 TODO 구현
3. 단위 테스트 작성
4. 통합 테스트 작성
5. Spring AI 연동 (RAG 파이프라인)
6. 보안 및 권한 관리 강화

## 참고

- JWT 설정: `global/config/JwtTokenProvider.java`
- Spring Security 설정: `global/config/SecurityConfig.java`
- S3 설정: `global/config/S3Config.java`
- 비동기 설정: `global/config/AsyncConfig.java`
- 전역 예외 처리: `global/exception/GlobalExceptionHandler.java`
