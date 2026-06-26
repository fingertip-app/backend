-- 정산 배치용 인덱스: 상태와 업데이트 시간으로 예약 필터링
CREATE INDEX idx_reservation_status_updated_at ON reservations(status, updated_at);

-- 장인별 정산 조회 최적화: 정렬 포함 복합 인덱스
CREATE INDEX idx_settlement_artisan_created ON settlements(artisan_id, created_at DESC);

-- 기존 단일 인덱스 제거 (복합 인덱스로 대체)
DROP INDEX IF EXISTS idx_settlement_artisan_id;
