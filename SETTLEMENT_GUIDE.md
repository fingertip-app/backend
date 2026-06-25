# 정산 시스템 가이드 (Mock Payment)

> 공모전용 Mock 결제 시스템 - 실제 PG 연동 없음

---

## 시스템 구조

### 플로우
```
1. 사용자 예약 생성 (PENDING)
2. 장인 승인 (APPROVED)
3. Mock 결제 처리 (PAID) → Payment 생성
4. QR 코드 스캔 (COMPLETED) → 정산 대상
5. 정산 배치 실행 → Settlement 생성
6. 송금 완료 처리 → Settlement COMPLETED
```

---

## 엔티티 구조

### Payment (결제 내역)
- Mock 결제 정보 저장
- 자동 완료 처리 (실제 PG 없음)
- 예약 1:1 관계

### Settlement (정산)
- 장인별 정산 내역
- 플랫폼 수수료 10% 자동 계산
- 정산 기간별 그룹핑

### SettlementItem (정산 항목)
- Settlement에 포함된 예약 목록
- 예약별 수수료 계산

---

## API 엔드포인트

### 1. 결제 처리 (Mock)
```http
POST /reservations/{reservationId}/payment?paymentMethod=CARD
```

**파라미터:**
- `paymentMethod` (default: CARD): 결제 수단

**결과:**
- Payment 자동 생성 및 완료
- Reservation 상태 → PAID
- QR 코드 자동 생성

---

### 2. 정산 생성 (관리자용 배치)
```http
POST /api/v1/settlements/batch?startDate=2026-06-01&endDate=2026-06-30
```

**동작:**
- 기간 내 COMPLETED 상태 예약 조회
- 장인별로 그룹핑
- Settlement + SettlementItem 생성
- 플랫폼 수수료 10% 자동 차감

---

### 3. 정산 완료 처리 (관리자용)
```http
POST /api/v1/settlements/{settlementId}/complete
```

**동작:**
- Settlement 상태 → COMPLETED
- Mock 송금 참조번호 자동 생성

---

### 4. 장인별 정산 조회
```http
GET /api/v1/settlements/artisan/{artisanId}
```

**응답 예시:**
```json
{
  "data": [
    {
      "id": 1,
      "artisanId": 1,
      "artisanName": "김장인",
      "settlementStartDate": "2026-06-01",
      "settlementEndDate": "2026-06-30",
      "totalRevenue": 500000,
      "platformFeeRate": 0.10,
      "platformFee": 50000,
      "settlementAmount": 450000,
      "status": "COMPLETED",
      "completedAt": "2026-07-01T10:00:00",
      "createdAt": "2026-07-01T09:00:00"
    }
  ],
  "message": "success"
}
```

---

### 5. 정산 상세 조회
```http
GET /api/v1/settlements/{settlementId}
```

**응답 예시:**
```json
{
  "data": {
    "settlement": {
      "id": 1,
      "artisanName": "김장인",
      "totalRevenue": 500000,
      "platformFee": 50000,
      "settlementAmount": 450000,
      "status": "COMPLETED"
    },
    "items": [
      {
        "id": 1,
        "reservationId": 101,
        "experienceTitle": "전통 도자기 체험",
        "amount": 100000,
        "feeAmount": 10000,
        "settlementAmount": 90000
      },
      {
        "id": 2,
        "reservationId": 102,
        "experienceTitle": "한지 공예 체험",
        "amount": 150000,
        "feeAmount": 15000,
        "settlementAmount": 135000
      }
    ]
  },
  "message": "success"
}
```

---

### 6. 대기 중인 정산 목록 (관리자용)
```http
GET /api/v1/settlements/pending
```

---

## 정산 정책

### 플랫폼 수수료
- **10% 고정**
- Settlement 생성 시 자동 계산
- 변경 가능 (코드 수정 필요)

### 정산 주기
- **수동 배치** (관리자가 API 호출)
- 주간/월간 등 자유롭게 설정 가능

### 정산 대상
- **COMPLETED 상태 예약만 포함**
- QR 코드 스캔 완료된 예약
- 중복 정산 방지 (SettlementItem 존재 여부 체크)

---

## 테스트 시나리오

### 1. 예약 → 결제 → 체험 완료 → 정산
```bash
# 1. 예약 생성
POST /reservations
{ "experienceId": 1, "scheduleId": 1, "numberOfParticipants": 2 }

# 2. 장인 승인
POST /reservations/1/approve

# 3. 결제 처리 (Mock)
POST /reservations/1/payment?paymentMethod=CARD

# 4. QR 스캔 (체험 완료)
POST /api/v1/qr/verify
{ "qrToken": "JANGINHAROU-RESERVATION:xxxx" }

# 5. 정산 생성 (관리자)
POST /api/v1/settlements/batch?startDate=2026-06-01&endDate=2026-06-30

# 6. 정산 완료 (관리자)
POST /api/v1/settlements/1/complete

# 7. 장인이 정산 내역 확인
GET /api/v1/settlements/artisan/1
```

---

## 데이터베이스 테이블

### payments
- 결제 내역
- reservation_id (UNIQUE)
- status: PENDING, COMPLETED, CANCELLED, REFUNDED

### settlements
- 정산 내역
- artisan_id
- status: PENDING, PROCESSING, COMPLETED, FAILED

### settlement_items
- 정산 항목
- settlement_id + reservation_id

---

## 주의사항

⚠️ **Mock 결제 시스템**
- 실제 PG 연동 없음
- 결제 즉시 완료 처리
- 공모전 시연용

⚠️ **정산 배치**
- 자동 스케줄러 없음 (필요 시 추가 가능)
- 관리자가 수동으로 API 호출

⚠️ **플랫폼 수수료**
- 현재 10% 고정
- 변경 필요 시: `SettlementService.DEFAULT_PLATFORM_FEE_RATE` 수정
