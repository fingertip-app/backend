# AI Recommendation API DTO Draft

## Purpose

AI 추천 탭에서 프론트가 Spring으로 보낼 요청과 Spring이 프론트로 내려줄 응답 구조 초안입니다.

이 API는 앱-facing Spring API입니다. 프론트는 FastAPI를 직접 호출하지 않습니다.

## Decisions

| Item | Decision |
| --- | --- |
| 추천 API와 문화 설명 API | 분리 |
| 추천 요청 형태 | 구조화 필드 + `freeText` + 선택적 `conversationHistory` |
| 대화 상태 저장 | 서버 저장 없음. 프론트가 필요한 히스토리를 매 요청에 포함 |
| 체험 매칭 방식 | `Experience` 태그 기반 |
| FastAPI 역할 | AI 답변, 출처, 매칭 키워드, 추천 카테고리 생성 |
| Spring 역할 | FastAPI 응답을 받아 태그 기반 체험을 붙이고 프론트용 응답으로 변환 |
| AI 실패 처리 | 인기/기본 체험 fallback + 안내 문구 |

## Endpoints

```http
POST /api/v1/ai/recommendations
POST /api/v1/ai/explain
```

`/recommendations`는 사용자의 취향/상황 기반 추천입니다.

`/explain`은 카드뉴스나 특정 문화 키워드 기반 문화 설명입니다.

## Recommendation Request

### JSON

```json
{
  "freeText": "친구랑 조용히 만들 수 있는 체험을 하고 싶어요",
  "companionType": "FRIEND",
  "headCount": 2,
  "interests": ["공예", "매듭", "조용한 체험"],
  "region": "서울",
  "timePreference": "WEEKEND",
  "conversationHistory": [
    {
      "role": "USER",
      "content": "친구랑 특별한 날 갈 만한 체험을 찾고 있어요"
    },
    {
      "role": "ASSISTANT",
      "content": "같이 만드는 체험과 조용한 체험 중 어떤 쪽이 좋으세요?"
    }
  ],
  "locale": "ko"
}
```

### Field Spec

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `freeText` | string | no | 사용자가 자유롭게 입력한 문장. 최대 500자 |
| `companionType` | string | yes | 동행 관계 |
| `headCount` | integer | yes | 총 참여 인원. 1~20 |
| `interests` | array of string | yes | 취향/관심사/키워드. 1~5개 |
| `region` | string | no | 희망 지역. 없으면 전체 지역 기준 |
| `timePreference` | string | no | 희망 시간 조건 |
| `conversationHistory` | array | no | 대화형 추천에서 이전 대화 일부를 함께 전달 |
| `locale` | string | no | MVP 기본값 `ko` |

## Request Enums

### `companionType`

| Value | Meaning |
| --- | --- |
| `ALONE` | 혼자 |
| `FRIEND` | 친구 |
| `FAMILY` | 가족 |
| `COUPLE` | 연인 |
| `KIDS` | 아이 동반 |
| `FOREIGN_GUEST` | 외국인 지인 동반 |
| `OTHER` | 기타 |

### `timePreference`

| Value | Meaning |
| --- | --- |
| `MORNING` | 오전 |
| `AFTERNOON` | 오후 |
| `EVENING` | 저녁 |
| `WEEKDAY` | 평일 |
| `WEEKEND` | 주말 |
| `ANYTIME` | 상관없음 |

### `conversationHistory.role`

| Value | Meaning |
| --- | --- |
| `USER` | 사용자 메시지 |
| `ASSISTANT` | AI 응답 메시지 |

## Spring Handling

Spring은 프론트 요청을 그대로 FastAPI에 넘기지 않습니다.

Spring은 요청 필드를 자연어 query/context로 조립한 뒤 FastAPI `/api/v1/ai/explain`을 호출합니다.

### Query Assembly Example

```text
친구 2명이 서울에서 주말에 할 수 있는 전통 체험을 추천해줘.
관심사는 공예, 매듭, 조용한 체험이야.
추가 요청: 친구랑 조용히 만들 수 있는 체험을 하고 싶어요.
이전 대화 맥락: 같이 만드는 체험과 조용한 체험 중 조용한 체험을 선호함.
```

### FastAPI Response Input

FastAPI는 Spring에 다음 힌트를 반환합니다.

```json
{
  "answer": "친구와 함께라면 손으로 집중해서 만드는 공예 체험이 잘 맞아요.",
  "sources": [
    {
      "id": 7,
      "name": "매듭장",
      "source": "국가유산포털",
      "category": "공예"
    }
  ],
  "matchingKeywords": ["매듭장", "전통 매듭"],
  "recommendedCategories": ["공예"]
}
```

Spring은 이 값을 그대로 프론트에 넘기지 않고, 체험 태그 매칭에 사용합니다.

## Tag Matching Draft

DB 담당이 `Experience` 태그 구조를 구현합니다. Spring AI API는 아래 기준으로 붙일 수 있게 설계합니다.

### Matching Inputs

| Source | Used As |
| --- | --- |
| Request `interests` | 사용자 선호 태그 후보 |
| FastAPI `matchingKeywords` | 문화유산/키워드 태그 후보 |
| FastAPI `recommendedCategories` | 상위 분류 태그 후보 |
| Request `region` | 지역 필터 |
| Request `headCount` | 예약 가능 인원 필터 |

### Matching Policy

MVP 추천 정책:

1. 지역이 있으면 지역으로 1차 필터링
2. 인원 조건으로 예약 가능한 체험 필터링
3. `interests`, `matchingKeywords`, `recommendedCategories`를 태그 후보로 합침
4. 태그가 하나 이상 매칭되는 체험을 우선 노출
5. 매칭 결과가 부족하면 인기/최신 체험으로 보강

태그 후보 예:

```json
["공예", "매듭", "조용한 체험", "매듭장", "전통 매듭"]
```

## Recommendation Response

### Success Response: 200 OK

```json
{
  "answer": "친구와 함께라면 손으로 집중해서 만드는 전통 공예 체험이 잘 맞아요.",
  "sources": [
    {
      "id": 7,
      "name": "매듭장",
      "source": "국가유산포털",
      "category": "공예"
    }
  ],
  "matchingKeywords": ["매듭장", "전통 매듭"],
  "recommendedTags": ["공예", "매듭", "조용한 체험"],
  "recommendedExperiences": [
    {
      "id": 12,
      "title": "전통 매듭 팔찌 만들기",
      "summary": "차분하게 손으로 매듭을 엮어 나만의 팔찌를 만드는 체험",
      "thumbnailUrl": "https://cdn.janginharu.com/experiences/12.jpg",
      "region": "서울",
      "location": "서울 종로구",
      "price": 35000,
      "durationMinutes": 90,
      "tags": ["공예", "매듭", "실내", "친구"],
      "matchReason": "매듭장 키워드와 공예 태그가 추천 조건과 잘 맞습니다."
    }
  ],
  "fallback": false,
  "message": null
}
```

### Field Spec

| Field | Type | Description |
| --- | --- | --- |
| `answer` | string | AI 추천 설명 |
| `sources` | array | AI 답변 근거 출처 |
| `matchingKeywords` | array of string | FastAPI가 반환한 문화유산/키워드 힌트 |
| `recommendedTags` | array of string | Spring이 체험 매칭에 사용한 태그 후보 |
| `recommendedExperiences` | array | 실제 예약 가능한 체험 카드 목록 |
| `fallback` | boolean | AI 실패 또는 매칭 부족으로 fallback을 썼는지 여부 |
| `message` | string or null | fallback 또는 안내 문구 |

## Fallback Response

AI 또는 태그 매칭이 실패해도 추천 화면은 막지 않습니다.

```json
{
  "answer": "지금은 AI 추천이 잠시 원활하지 않아 인기 체험을 먼저 보여드려요.",
  "sources": [],
  "matchingKeywords": [],
  "recommendedTags": [],
  "recommendedExperiences": [
    {
      "id": 3,
      "title": "도자기 물레 체험",
      "summary": "처음이어도 쉽게 따라 할 수 있는 전통 도자 체험",
      "thumbnailUrl": "https://cdn.janginharu.com/experiences/3.jpg",
      "region": "서울",
      "location": "서울 종로구",
      "price": 40000,
      "durationMinutes": 120,
      "tags": ["공예", "도자기", "초보"],
      "matchReason": "인기 체험으로 먼저 추천합니다."
    }
  ],
  "fallback": true,
  "message": "AI 추천이 잠시 원활하지 않아 인기 체험을 먼저 보여드려요."
}
```

## Explain API Draft

문화 설명 API는 추천 API와 분리합니다.

```http
POST /api/v1/ai/explain
```

### Request

```json
{
  "query": "매듭장이 뭐예요?",
  "cardNewsId": 15,
  "locale": "ko"
}
```

### Response

```json
{
  "answer": "매듭장은 실을 꼬고 엮어 장식과 생활용품을 만드는 전통 공예입니다.",
  "sources": [
    {
      "id": 7,
      "name": "매듭장",
      "source": "국가유산포털",
      "category": "공예"
    }
  ],
  "matchingKeywords": ["매듭장"],
  "recommendedTags": ["공예", "매듭"],
  "relatedExperiences": [
    {
      "id": 12,
      "title": "전통 매듭 팔찌 만들기",
      "thumbnailUrl": "https://cdn.janginharu.com/experiences/12.jpg",
      "region": "서울",
      "price": 35000
    }
  ],
  "fallback": false,
  "message": null
}
```

## Validation Rules

| Field | Rule |
| --- | --- |
| `freeText` | 선택, 최대 500자 |
| `companionType` | enum whitelist |
| `headCount` | 1~20 |
| `interests` | 1~5개, 각 항목 1~50자 |
| `region` | 선택, 지역 whitelist |
| `timePreference` | 선택, enum whitelist |
| `conversationHistory` | 선택, 최대 10개 메시지, 각 메시지 500자 |
| `locale` | MVP는 `ko` |

## Error Responses

| HTTP Status | Situation | App Handling |
| ---: | --- | --- |
| `400` | 요청값 검증 실패 | 입력값 오류 표시 |
| `401` | 인증 필요 API에서 미인증 | 로그인 유도 |
| `429` | 요청 과다 | 잠시 후 다시 시도 안내 |
| `503` | fallback도 만들 수 없는 서버 장애 | AI 기능 일시 불가 안내 |

대부분의 AI/OpenAI/FastAPI 장애는 `200 OK + fallback=true`로 처리해 추천 화면 흐름을 유지합니다.

## Implementation TODO

### Backend

- [ ] `AiRecommendationRequest` 작성
- [ ] `AiRecommendationResponse` 작성
- [ ] `RecommendedExperienceResponse` 작성
- [ ] `AiExplainRequest` / `AiExplainResponse` 작성
- [ ] `AiRecommendationController` 작성
- [ ] `AiRecommendationService`에서 FastAPI query 조립
- [ ] FastAPI 응답을 Spring 응답으로 변환
- [ ] 태그 DB 구현 후 체험 매칭 쿼리 연결
- [ ] AI 실패 시 인기/기본 체험 fallback 연결

### Frontend

- [ ] `AiRecommendationRequest` 기준 입력 UI 확인
- [ ] `recommendedExperiences` 카드 필드 부족 여부 확인
- [ ] `fallback=true` 안내 문구 표시 방식 확인
- [ ] 추천 API와 설명 API 화면 흐름 분리

### DB

- [ ] `Experience` 태그 저장 방식 확정
- [ ] 태그 seed 또는 관리자 입력 방식 확정
- [ ] 태그 검색/매칭 인덱스 검토
