-- 배너 테이블 생성
CREATE TABLE banners (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(500),
    tag VARCHAR(50) NOT NULL,
    image_url TEXT,
    banner_type VARCHAR(50),
    display_order INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT true,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_banners_active_type ON banners(is_active, banner_type);
CREATE INDEX idx_banners_display_order ON banners(display_order);

-- 초기 샘플 데이터 (히어로 배너)
INSERT INTO banners (title, subtitle, tag, image_url, banner_type, display_order, is_active, created_at, updated_at)
VALUES
    ('전통 한지 공예 체험', '천년의 기술을 배워보세요', '한지공예', 'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=800&q=80', 'HERO', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('전통 도자기 만들기', '손끝에서 피어나는 예술', '도예', 'https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=800&q=80', 'HERO', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('전통 차 문화 체험', '차 한 잔에 담긴 여유', '다도', 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=800&q=80', 'HERO', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
