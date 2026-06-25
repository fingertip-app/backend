-- V14: artisans 테이블에 위치 정보 필드 추가
-- 장인의 위치를 지도에 표시하기 위한 주소, 위도, 경도 필드

ALTER TABLE artisans
    ADD COLUMN IF NOT EXISTS address TEXT,
    ADD COLUMN IF NOT EXISTS latitude NUMERIC(10, 7),
    ADD COLUMN IF NOT EXISTS longitude NUMERIC(10, 7);

COMMENT ON COLUMN artisans.address IS '장인 공방 주소';
COMMENT ON COLUMN artisans.latitude IS '위도 (소수점 7자리)';
COMMENT ON COLUMN artisans.longitude IS '경도 (소수점 7자리)';
