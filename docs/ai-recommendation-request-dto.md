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

## MVP Decision

- 필수 입력은 `companionType`, `partySize`, `interests`만 둡니다.
- `region`, `preferredDate`, `preferredTimeSlot`, `freeText`는 선택값으로 둡니다.
- 로그인 사용자 정보는 이 DTO에 넣지 않습니다. 필요하면 Spring 인증 컨텍스트에서 별도로 읽습니다.
- 가격대는 MVP 초안에서 제외합니다. 실제 체험 데이터에 가격 필터 품질이 충분할 때 추가합니다.
