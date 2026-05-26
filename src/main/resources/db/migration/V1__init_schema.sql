-- Flyway migration: V1__init_schema.sql
-- Python Alembic 마이그레이션과 동일한 스키마

-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- users
CREATE TABLE users (
    id                    BIGSERIAL PRIMARY KEY,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    provider              VARCHAR(50)  NOT NULL,
    nickname              VARCHAR(100) NOT NULL,
    profile_image_url     TEXT,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE user_preferred_categories (
    user_id  BIGINT      NOT NULL REFERENCES users(id),
    category VARCHAR(100) NOT NULL
);

CREATE TABLE user_preferred_content_types (
    user_id      BIGINT      NOT NULL REFERENCES users(id),
    content_type VARCHAR(100) NOT NULL
);

-- artisans
CREATE TABLE artisans (
    id                    BIGSERIAL    PRIMARY KEY,
    user_id               BIGINT       NOT NULL REFERENCES users(id),
    name                  VARCHAR(100) NOT NULL,
    heritage_category     VARCHAR(100) NOT NULL,
    certification_number  VARCHAR(100) UNIQUE,
    bio                   TEXT,
    profile_image_url     TEXT,
    intro_video_url       TEXT,
    is_verified           BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- experiences
CREATE TABLE experiences (
    id                BIGSERIAL      PRIMARY KEY,
    artisan_id        BIGINT         NOT NULL REFERENCES artisans(id),
    title             VARCHAR(255)   NOT NULL,
    description       TEXT           NOT NULL,
    cultural_story    TEXT,
    category          VARCHAR(100)   NOT NULL,
    price             NUMERIC(10, 2) NOT NULL,
    duration_minutes  INTEGER        NOT NULL,
    max_participants  INTEGER        NOT NULL,
    difficulty        VARCHAR(20),
    location_address  TEXT,
    location_lat      NUMERIC(10, 7),
    location_lng      NUMERIC(10, 7),
    is_active         BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE experience_supported_languages (
    experience_id BIGINT      NOT NULL REFERENCES experiences(id),
    language      VARCHAR(50) NOT NULL
);

-- experience_schedules
CREATE TABLE experience_schedules (
    id               BIGSERIAL PRIMARY KEY,
    experience_id    BIGINT    NOT NULL REFERENCES experiences(id),
    scheduled_at     TIMESTAMP NOT NULL,
    available_slots  INTEGER   NOT NULL,
    is_active        BOOLEAN   NOT NULL DEFAULT TRUE
);

-- bookings
CREATE TABLE bookings (
    id              BIGSERIAL      PRIMARY KEY,
    user_id         BIGINT         NOT NULL REFERENCES users(id),
    experience_id   BIGINT         NOT NULL REFERENCES experiences(id),
    schedule_id     BIGINT         NOT NULL REFERENCES experience_schedules(id),
    participants    INTEGER        NOT NULL,
    total_price     NUMERIC(10, 2) NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'pending',
    request_message TEXT,
    qr_code         TEXT,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- reviews
CREATE TABLE reviews (
    id              BIGSERIAL     PRIMARY KEY,
    booking_id      BIGINT        NOT NULL UNIQUE REFERENCES bookings(id),
    user_id         BIGINT        NOT NULL REFERENCES users(id),
    experience_id   BIGINT        NOT NULL REFERENCES experiences(id),
    rating          INTEGER       NOT NULL,
    content         TEXT,
    new_learnings   TEXT,
    sentiment_score NUMERIC(3, 2),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE review_image_urls (
    review_id BIGINT NOT NULL REFERENCES reviews(id),
    image_url TEXT   NOT NULL
);

CREATE TABLE review_keywords (
    review_id BIGINT      NOT NULL REFERENCES reviews(id),
    keyword   VARCHAR(100) NOT NULL
);

-- card_news
CREATE TABLE card_news (
    id             BIGSERIAL    PRIMARY KEY,
    title          VARCHAR(255) NOT NULL,
    content_type   VARCHAR(50)  NOT NULL,
    image_url      TEXT,
    ai_explanation TEXT,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE card_news_category_tags (
    card_news_id BIGINT      NOT NULL REFERENCES card_news(id),
    tag          VARCHAR(100) NOT NULL
);

-- card_news_experiences
CREATE TABLE card_news_experiences (
    id               BIGSERIAL     PRIMARY KEY,
    card_news_id     BIGINT        NOT NULL REFERENCES card_news(id),
    experience_id    BIGINT        NOT NULL REFERENCES experiences(id),
    similarity_score NUMERIC(4, 3),
    UNIQUE (card_news_id, experience_id)
);

-- wishlists
CREATE TABLE wishlists (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT    NOT NULL REFERENCES users(id),
    experience_id BIGINT    NOT NULL REFERENCES experiences(id),
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, experience_id)
);

-- notifications
CREATE TABLE notifications (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    title      VARCHAR(255) NOT NULL,
    body       TEXT         NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- heritage_items (RAG 벡터 DB)
CREATE TABLE heritage_items (
    id          BIGSERIAL    PRIMARY KEY,
    source      VARCHAR(100) NOT NULL,
    external_id VARCHAR(100) UNIQUE,
    name        VARCHAR(255) NOT NULL,
    category    VARCHAR(100),
    description TEXT,
    history     TEXT,
    embedding   vector(1536),          -- pgvector: OpenAI text-embedding-3-small
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 벡터 유사도 검색용 IVFFlat 인덱스
CREATE INDEX ON heritage_items USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
