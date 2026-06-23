# Reservation Schedule Policy

## Time Slot

- 예약은 `experienceId`가 아니라 `scheduleId` 기준으로 생성한다.
- `experience_schedules.scheduled_at`이 실제 체험 일시다.
- `experience_schedules.available_slots`가 해당 회차의 총 예약 가능 인원이다.
- 예약 생성 시 요청의 `experienceId`와 `scheduleId`가 같은 체험에 속하는지 검증한다.

## Capacity Holding Statuses

아래 상태의 예약 인원은 잔여 좌석 계산에서 차감한다.

- `PENDING`
- `APPROVED`
- `PAID`
- `CONFIRMED`

아래 상태의 예약 인원은 잔여 좌석 계산에서 제외한다.

- `REJECTED`
- `CANCELLED`
- `COMPLETED`

## State Transition

- 생성: `PENDING`
- 장인 승인: `PENDING -> APPROVED`
- 장인 거절: `PENDING -> REJECTED`
- 결제 처리: `APPROVED -> PAID`
- 최종 확정: `PAID -> CONFIRMED`
- 취소: `PENDING`, `APPROVED`, `PAID`, `CONFIRMED -> CANCELLED`

## Concurrency

- 예약 생성 트랜잭션에서 `experience_schedules` row를 `PESSIMISTIC_WRITE` lock으로 조회한다.
- lock을 잡은 뒤 같은 `scheduleId`의 capacity holding 예약 인원 합계를 계산한다.
- `bookedParticipants + requestedParticipants > availableSlots`이면 예약 생성을 거절한다.
