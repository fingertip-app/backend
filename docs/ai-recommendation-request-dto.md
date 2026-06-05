# AI Recommendation Request DTO Draft

## Purpose

AI 추천 탭에서 사용자가 입력한 상황을 Spring이 받아 FastAPI `/api/v1/ai/explain` 요청용 자연어 query/context로 변환하기 위한 앱용 요청 DTO 초안입니다.

## Endpoint Draft

```http
POST /api/v1/ai/recommendations
```

## Request Body

```json
{
  "companionType": "FRIEND",
  "partySize": 2,
  "interests": ["도자기", "전통 매듭", "조용한 체험"],
  "region": "서울",
  "preferredDate": "2026-06-28",
  "preferredTimeSlot": "AFTERNOON",
  "freeText": "친구랑 특별한 하루를 보내고 싶어요",
  "locale": "ko"
}
```

## Field Spec

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `companionType` | string | yes | 누구와 가는지. 추천 톤과 체험 유형을 결정하는 핵심 조건 |
| `partySize` | integer | yes | 총 참여 인원. 최소 1 |
| `interests` | array of string | yes | 취향/관심사/키워드. 최소 1개 |
| `region` | string | no | 희망 지역. 없으면 전체 지역 기준 |
| `preferredDate` | string | no | 희망 날짜. `YYYY-MM-DD` |
| `preferredTimeSlot` | string | no | 희망 시간대 |
| `freeText` | string | no | 사용자가 자유롭게 입력한 문장 |
| `locale` | string | no | 응답 언어. MVP는 `ko` 기본값 |

## Enum Draft

### `companionType`

| Value | Meaning | UI Example |
| --- | --- | --- |
| `ALONE` | 혼자 | 혼자 조용히 |
| `FRIEND` | 친구 | 친구랑 특별한 날 |
| `FAMILY` | 가족 | 가족 나들이 |
| `COUPLE` | 연인 | 연인과 함께 |
| `KIDS` | 아이 동반 | 아이와 함께 |
| `FOREIGN_GUEST` | 외국인 지인 동반 | 외국 친구 데려오기 |
| `OTHER` | 기타 | 직접 입력 |

### `preferredTimeSlot`

| Value | Meaning |
| --- | --- |
| `MORNING` | 오전 |
| `AFTERNOON` | 오후 |
| `EVENING` | 저녁 |
| `ANYTIME` | 상관없음 |

## Spring Handling Draft

Spring은 이 요청을 그대로 FastAPI에 넘기지 않고, 아래처럼 추천 query를 조립합니다.

```text
친구 2명이 서울에서 오후에 할 수 있는 전통 체험을 추천해줘.
관심사는 도자기, 전통 매듭, 조용한 체험이야.
추가 요청: 친구랑 특별한 하루를 보내고 싶어요.
```

FastAPI 응답의 `matchingKeywords`, `recommendedCategories`는 Spring에서 체험 목록을 필터링하거나 랭킹하는 조건으로 사용합니다.

## Validation Rules

| Field | Rule | Example |
| --- | --- | --- |
| `companionType` | Enum whitelist only: `ALONE`, `FRIEND`, `FAMILY`, `COUPLE`, `KIDS`, `FOREIGN_GUEST`, `OTHER` | ✅ `FRIEND` ❌ `FRIENDS` |
| `partySize` | 1-20 정수 범위 | ✅ `2` ❌ `0` ❌ `50` |
| `interests` | 배열 크기 1-5, 각 항목 1-50자 | ✅ `["도자기", "매듭"]` ❌ `[]` ❌ `["x"*51]` |
| `region` | Whitelist: `[전국, 서울, 경기, 강원, 충청, 전라, 경상, 제주]` (기본값 제외 시 필수) | ✅ `서울` ❌ `서울시` |
| `preferredDate` | ISO8601 형식 `YYYY-MM-DD`, 현재일 이후만 | ✅ `2026-06-28` ❌ `2026-6-28` ❌ `2026-06-01` |
| `preferredTimeSlot` | Enum whitelist only: `MORNING`, `AFTERNOON`, `EVENING`, `ANYTIME` | ✅ `AFTERNOON` ❌ `MORNING_AFTERNOON` |
| `freeText` | 최대 500자, HTML/SQL 이스케이프 | ✅ `친구랑 특별한 하루` ❌ `<script>...` |
| `locale` | Enum whitelist only: `ko`, `en`, `ja` (기본값 `ko`) | ✅ `ko` ❌ `korean` |

**검증 실패 시:** 400 Bad Request + validation error array 반환

---

## Spring Handling Detail

### Query Assembly Logic

Spring은 다음 순서로 추천 query를 조립합니다:

```text
[companionType 톤] [partySize]명이 
[region 없으면 생략, 있으면 "{region}에서"] 
[preferredTimeSlot 없으면 생략, 있으면 "{시간대}에"] 
할 수 있는 
[interests 첫 번째 항목] 중심 전통 체험을 추천해줘.

추가 관심사: [interests 2-5번째를 쉼표로 연결].
[preferredDate 없으면 생략, 있으면 "선호 날짜: {날짜}"]
[freeText 없으면 생략, 있으면 "추가 요청: {freeText}"]
```

**예시:**
```text
입력: companionType=FRIEND, partySize=2, interests=[도자기, 매듭, 조용한 체험], 
      region=서울, preferredTimeSlot=AFTERNOON, freeText=친구랑 특별한 하루

조립: "친구 2명이 서울에서 오후에 할 수 있는 도자기 중심 전통 체험을 추천해줘. 
      추가 관심사: 매듭, 조용한 체험. 추가 요청: 친구랑 특별한 하루를 보내고 싶어요."
```

### FastAPI 응답 활용

FastAPI `/api/v1/ai/explain`의 응답을 Spring에서 처리:

1. **matchingKeywords** → 체험 검색/필터링 기준
   - AND 조건: 모든 키워드를 포함하는 체험만 추천 (엄격)
   - OR 조건: 1개 이상 키워드 매칭 (관대)
   - **MVP는 OR 조건** (더 많은 체험 제시)

2. **recommendedCategories** → 체험 카테고리 필터링
   - 응답의 카테고리와 앱의 Experience.category 매칭
   - 매칭되는 체험을 우선 순위 높게 표시

3. **answer** → UI에 생성된 해설 텍스트 표시

---

## Response Spec

### Success Response: 200 OK

FastAPI 응답을 그대로 앱에 반환:

```json
{
  "answer": "도자기는 손으로 빚고 굽는 전통 공예로...",
  "sources": [
    {
      "id": 7,
      "name": "이천 도자마을",
      "source": "국가유산포털",
      "category": "공예"
    }
  ],
  "matchingKeywords": ["도자기", "전통 공예"],
  "recommendedCategories": ["공예", "생활문화"]
}
```

### No Search Results: 200 OK

FastAPI가 sources=[]를 반환한 경우, Spring도 그대로 전달:

```json
{
  "answer": "관련 문화유산 자료를 찾지 못했습니다. 다른 키워드로 검색해 주세요.",
  "sources": [],
  "matchingKeywords": [],
  "recommendedCategories": []
}
```

**앱 처리:** "검색 결과 없음" 상태로 표시

---

## Error Responses

| HTTP Status | Situation | Response Body | App Handling |
| ---: | --- | --- | --- |
| `400` | companionType 유효하지 않음 | `{"detail": "Invalid companionType: XYZ"}` | 입력값 오류 표시 |
| `400` | partySize < 1 또는 > 20 | `{"detail": "partySize must be 1-20"}` | 입력값 오류 표시 |
| `400` | interests 빈 배열 또는 > 5개 | `{"detail": "interests array size must be 1-5"}` | 입력값 오류 표시 |
| `400` | interests 항목 > 50자 | `{"detail": "Each interest must be 1-50 chars"}` | 입력값 오류 표시 |
| `400` | freeText > 500자 | `{"detail": "freeText max 500 chars"}` | 입력값 오류 표시 |
| `400` | preferredDate 형식 오류 | `{"detail": "preferredDate must be YYYY-MM-DD"}` | 입력값 오류 표시 |
| `400` | preferredDate가 과거 | `{"detail": "preferredDate must be today or later"}` | 입력값 오류 표시 |
| `400` | region 유효하지 않음 | `{"detail": "Invalid region. Must be in: [...]"}` | 입력값 오류 표시 |
| `401` | 미인증 요청 | `{"detail": "Authentication required"}` | 로그인 유도 |
| `429` | 너무 빈번한 요청 (rate limit) | `{"detail": "Too many requests. Wait 60s"}` | "잠시 후 다시 시도" 안내 |
| `503` | FastAPI 또는 OpenAI 불가 | `{"detail": "AI service unavailable"}` | "AI 기능 일시 불가" + fallback |

---

## Security Policy

### 입력값 검증

- **companionType, preferredTimeSlot, locale**: Enum whitelist 검증 (다른 값 거부)
- **partySize**: 정수 범위 검증 (1-20)
- **interests**: 배열 크기 (1-5), 각 항목 길이 (1-50자)
- **freeText**: HTML/SQL escape 처리, 최대 500자
- **preferredDate**: ISO8601 형식 검증, 과거 날짜 거부
- **region**: whitelist 지역만 허용

### 민감 정보 필터링

- **freeText** 검사: 전화번호, 이메일 패턴 감지 (포함 시 경고/제외 검토)
- **로그**: freeText 원문 저장 금지 (해시만 기록)

### Rate Limiting

- **같은 사용자**: 60초당 최대 10개 요청
- **같은 IP**: 60초당 최대 30개 요청
- **미인증 사용자**: 60초당 최대 3개 요청

**저장:** Redis에 요청 카운트 저장 (TTL 60초)

### 인증

- **로그인 필수**: JWT 토큰 필수 (Authorization header)
- **토큰 검증**: Spring 인증 필터에서 처리
- **사용자 정보**: JWT claim에서 userId 추출 (DTO에 넣지 않음)

### 감시/감사

- **요청 로깅**: userId, companionType, interests, 응답 상태 (freeText는 제외)
- **에러 모니터링**: 400 오류 > 50/시간 시 알림
- **FastAPI 호출**: X-Internal-Api-Key 필수 (이미 구현됨)

---

## Implementation TODO

### Backend (Spring)

- [ ] RecommendationRequest DTO 작성 (Jakarta Validation 어노테이션 포함)
- [ ] RecommendationController 구현
- [ ] RecommendationService에서 query 조립 로직 구현
- [ ] FastApiClient 호출 및 응답 처리
- [ ] input validation + error response 처리
- [ ] rate limiting 미들웨어/인터셉터 추가
- [ ] 민감 정보 필터링 로직 추가
- [ ] 요청 로깅 추가
- [ ] 단위 테스트 (validation, query assembly, error handling)
- [ ] 통합 테스트 (FastAPI 모의 + 전체 흐름)

### Frontend

- [ ] AI 추천 탭 UI 구현
  - [ ] companionType 선택 (6가지 + OTHER)
  - [ ] partySize 입력 (숫자 스피너, 1-20)
  - [ ] interests 입력 (태그/칩, 최대 5개)
  - [ ] region 선택 (드롭다운, 선택사항)
  - [ ] preferredDate 선택 (날짜 피커, 선택사항)
  - [ ] preferredTimeSlot 선택 (라디오, 선택사항)
  - [ ] freeText 입력 (텍스트 영역, 500자 제한 표시)
- [ ] Spring API 연동 (POST /api/v1/ai/recommendations)
- [ ] 응답 표시 (answer + sources + keywords + categories)
- [ ] 검색 미스 표시 (sources=[])
- [ ] 로딩/에러 상태 표시
- [ ] 입력값 클라이언트 검증 (UX)

---

## MVP Decision

- ✅ 필수 입력: `companionType`, `partySize`, `interests`
- ✅ 선택 입력: `region`, `preferredDate`, `preferredTimeSlot`, `freeText`
- ✅ 사용자 정보: JWT에서 읽음 (DTO 제외)
- ✅ 가격대: MVP 제외 (향후 Experience.priceRange 추가 후 검토)
- ✅ 인증: 로그인 필수 (401 반환)
- ✅ 다국어: MVP는 `ko` 기본값만 (향후 `en`, `ja` 추가)

---

## Future Scope (MVP 이후)

- 가격대 필터 추가 (Experience.minPrice, maxPrice 기준)
- 접근성 필터 (휠체어 접근, 시각 장애 지원 등)
- 체험 난이도 필터 (초보/중급/고급)
- 사용자 위치 기반 추천 (현재 위치로부터 거리 필터)
- 개인화 추천 (사용자 과거 예약 이력 기반)
- 실시간 재고 필터 (선호 날짜에 가능한 체험만)
- 후기/평점 기반 순위 조정
