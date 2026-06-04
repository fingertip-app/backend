# Spring Backend TODO - 2026-06-28 MVP

## 책임 범위

`backend/`는 앱이 직접 호출하는 Spring Boot 메인 API 서버입니다. 인증, 사용자, 장인, 체험, 예약, 결제, 알림, 관리자 API를 담당합니다. RAG, 번역, 후기 요약은 직접 구현하지 않고 `backend_python/` 내부 API를 호출합니다.

## MVP 완료 기준

- 앱은 Spring API 하나만 호출해 로그인, 체험 탐색, AI 해설, 예약, 결제, QR 확인 흐름을 완료할 수 있다.
- 예약 상태 전이 `PENDING -> APPROVED -> PAID -> CONFIRMED`와 `REJECTED`, `CANCELLED`가 검증된다.
- Toss Payments 테스트 webhook은 서명/중복 호출/잘못된 상태 전이를 처리한다.
- Spring에서 FastAPI 해설 API를 호출하고 timeout 또는 실패 시 정의된 오류를 반환한다.
- 관리자 역할로 장인 인증 승인과 카드뉴스 관리가 가능하다.

## 일정

### 5/26 - 5/31: 계약 동결과 기반 정리

- [ ] [공통/영진] 외부 API 경로, 오류 포맷, 인증 방식, Spring-FastAPI 내부 요청/응답 계약 확정
- [ ] [공통/지현] 예약·결제 상태값과 전이 권한 표 확정
- [ ] [지현] Flyway 기준 Spring 소유 테이블 확정: users, artisans, experiences, schedules, reservations, payments, reviews, card_news, notifications
- [ ] [지현] 로컬 Docker/PostgreSQL/Redis 실행과 GitHub Actions 빌드·테스트 기본 작업 구성
- [x] [영진] FastAPI Client skeleton 완료
  - [x] commit `4560c65` 기준 client 구조 추가
  - [x] 설정값/timeout/error mapping 기본 형태 작성
  - [x] 이후 502 retry와 테스트 기준 정의 대상으로 분리

완료 기준: Swagger/OpenAPI 초안, ERD, 상태 전이표, AI 내부 API 계약이 팀 검토를 통과한다.

### 6/1 - 6/7: FastAPI 연동 안정화와 테스트 기준

- [ ] [영진] FastAPI 502/timeout 재시도 정책 구현
  - [ ] 502, connection reset, read timeout별 retry 여부 구분
  - [ ] 최대 재시도 횟수와 backoff 값 설정화
  - [ ] retry 후 최종 실패 시 프론트로 내려갈 오류 포맷 고정
- [ ] [영진] Controller/Service 단위 테스트 기준과 공통 fixture 정의
  - [ ] AI client mock fixture 작성
  - [ ] timeout/fallback/error mapping 테스트 작성
  - [ ] 테스트 네이밍과 Given-When-Then 기준 정리
- [ ] [지현] JWT 필터와 역할 기반 접근 제어 구현: user, artisan, admin
  - [ ] 사용자/장인/관리자 권한별 접근 가능 API 정리
  - [ ] 인증 실패와 권한 실패 응답 코드 분리
- [ ] [지현] 사용자 로그인/프로필, 장인 신청/승인, 체험 목록/상세 API 구현
  - [ ] 로그인/프로필 기본 API
  - [ ] 장인 신청/관리자 승인 API
  - [ ] 체험 목록/상세 조회 API
- [ ] [지현] 체험 일정 및 예약 가능 인원 모델/마이그레이션 구현
  - [ ] schedule/time slot 모델 확정
  - [ ] 예약 가능 인원 차감 기준 정리

완료 기준: 인증된 사용자가 체험 목록과 상세를 조회하고, 관리자가 장인을 승인할 수 있다.

### 6/8 - 6/14: AI 프록시와 예약 API

- [ ] [영진] `AI 해설 + 출처 + 관련 체험` Spring 외부 API 구현 및 FastAPI 연결
  - [ ] 앱용 AI 해설 API request/response 작성
  - [ ] FastAPI explain 응답을 Spring DTO로 변환
  - [ ] 출처, 매칭 키워드, 추천 카테고리 필드 노출
  - [ ] AI 실패 시 fallback/error response 처리
- [ ] [공통] [결정필요] 관련 체험 매칭 방식 결정
  - [ ] `Experience.category` 필드 추가 여부 결정
  - [ ] category 추가 시 migration, enum/string, 관리자 입력 방식 결정
  - [ ] category 미추가 시 tag/join table/search keyword 기반 매칭 방식 결정
  - [ ] 결정 결과를 FastAPI recommended_categories 응답과 맞춤
- [ ] [지현] 예약 생성, 목록, 장인 승인/거절 API와 동시 재고 검증 구현
  - [ ] 예약 생성 시 일정/정원 검증
  - [ ] 장인 승인/거절 상태 전이 검증
  - [ ] 중복 예약과 동시 요청 테스트 작성
- [ ] [지현] 카드뉴스 조회/관리 API와 관련 체험 연결 구현
  - [ ] 카드뉴스 목록/상세 API
  - [ ] 관리자 카드뉴스 생성/수정/삭제 API
  - [ ] 카드뉴스에서 AI 해설/관련 체험으로 이동할 식별자 제공
- [ ] [공통] 프론트 연동용 API 예시 응답과 에러 사례 제공
  - [ ] AI 해설 성공/실패 예시
  - [ ] 예약 성공/거절/정원 부족 예시
  - [ ] 카드뉴스/체험 상세 예시

완료 기준: 앱 API를 통해 카드뉴스/질문에서 실제 AI 해설과 예약 요청으로 이어진다.

### 6/15 - 6/21: 결제, QR, 후기와 알림

- [ ] [지현] Toss 테스트 결제 준비/승인/webhook 검증과 결제 중복 처리 구현
  - [ ] payment ready/confirm API 구현
  - [ ] webhook 서명 검증
  - [ ] 중복 webhook idempotency 처리
- [ ] [지현] 결제 완료 후 QR 확인서 생성/조회와 예약 확정 처리 구현
  - [ ] 결제 성공 시 `PAID -> CONFIRMED` 전이
  - [ ] QR payload와 만료/검증 기준 정의
  - [ ] QR 조회 API 구현
- [ ] [지현] 예약 승인·거절·확정 알림 비동기 발송 최소 구현
  - [ ] 알림 저장 모델 구현
  - [ ] 예약 상태 변경 이벤트와 연결
  - [ ] 푸시 연동 전 fallback 조회 API 제공
- [ ] [영진] 후기 생성 후 FastAPI 요약 요청/저장 흐름 및 번역 호출 구현
  - [ ] 후기 생성 API에서 요약 요청 트리거
  - [ ] 요약 실패 시 원문 저장 유지
  - [ ] 번역 결과 저장/조회 필드 정리
- [ ] [공통] 예약-결제-QR 및 AI 실패 fallback 통합 테스트 작성
  - [ ] 정상 결제 E2E
  - [ ] 결제 실패/중복 webhook
  - [ ] AI timeout/fallback

완료 기준: 테스트 결제 기반의 예약 전체 흐름과 후기 요약 결과 조회가 동작한다.

### 6/22 - 6/28: 배포와 시연 동결

- [ ] [지현] 테스트 배포 환경과 비밀값, 마이그레이션, health check 정리
  - [ ] 배포 환경변수 목록 정리
  - [ ] Flyway migration 검증
  - [ ] Spring/FastAPI health check 연결 확인
- [ ] [공통] 로그인 -> AI 해설 -> 체험 -> 예약 -> 승인 -> 결제 -> QR E2E 검증
  - [ ] 사용자 앱 시나리오 3회 반복
  - [ ] 장인 승인/거절 시나리오 포함
  - [ ] 관리자 카드뉴스 관리 시나리오 포함
- [ ] [공통] 결제 실패, AI timeout, 예약 재고 부족, 권한 실패 시나리오 점검
  - [ ] 프론트 표시 문구와 API 오류 코드 확인
  - [ ] fallback 응답이 시연 흐름을 막지 않는지 확인
  - [ ] 실제 기기 네트워크 지연 상황 점검
- [ ] [영진] Swagger/시연 데이터/장애 대응 절차 최종 검토
  - [ ] Swagger 예시 응답 최신화
  - [ ] AI fallback 대응 절차 문서화
  - [ ] FastAPI/Spring 통합 로그 확인

완료 기준: 실제 기기 시연 경로를 3회 연속 성공하고 6/28 시연 빌드를 동결한다.

## 마감 이후 항목

- 실결제 운영 정산/환불 자동화와 알림톡 정식 연동
- 자동 카드뉴스 제작/배포와 정교한 추천 랭킹
- 운영 관리자 화면, 모니터링, 공개 출시 보안 점검
