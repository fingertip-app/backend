ALTER TABLE users
    ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS provider VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nickname VARCHAR(100),
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

UPDATE users
SET provider = COALESCE(provider, 'supabase'),
    nickname = COALESCE(nickname, split_part(email, '@', 1))
WHERE provider_id IS NULL
   OR provider IS NULL
   OR nickname IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'social_id'
    ) THEN
        EXECUTE 'UPDATE users SET provider_id = COALESCE(provider_id, social_id) WHERE provider_id IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'social_provider'
    ) THEN
        EXECUTE 'UPDATE users SET provider = COALESCE(provider, social_provider, ''supabase'') WHERE provider IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'name'
    ) THEN
        EXECUTE 'UPDATE users SET nickname = COALESCE(nickname, name, split_part(email, ''@'', 1)) WHERE nickname IS NULL';
    END IF;
END $$;

ALTER TABLE users
    ALTER COLUMN provider SET NOT NULL,
    ALTER COLUMN nickname SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_provider_id
    ON users(provider_id)
    WHERE provider_id IS NOT NULL;

ALTER TABLE artisans
    ADD COLUMN IF NOT EXISTS name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS heritage_category VARCHAR(100),
    ADD COLUMN IF NOT EXISTS certification_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS intro_video_url TEXT,
    ADD COLUMN IF NOT EXISTS certification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE artisans
SET certification_status = COALESCE(certification_status, 'PENDING');

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'artisans'
          AND column_name = 'intangible_heritage_name'
    ) THEN
        EXECUTE 'UPDATE artisans SET name = COALESCE(name, intangible_heritage_name) WHERE name IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'artisans'
          AND column_name = 'intangible_heritage_type'
    ) THEN
        EXECUTE 'UPDATE artisans SET heritage_category = COALESCE(heritage_category, intangible_heritage_type) WHERE heritage_category IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'artisans'
          AND column_name = 'verification_status'
    ) THEN
        EXECUTE 'UPDATE artisans SET certification_status = COALESCE(certification_status, verification_status, ''PENDING'') WHERE certification_status IS NULL';
    END IF;
END $$;

ALTER TABLE artisans
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN heritage_category SET NOT NULL;

UPDATE artisans
SET certification_status = UPPER(certification_status);

UPDATE artisans
SET is_verified = certification_status = 'APPROVED',
    verified_at = CASE
        WHEN certification_status = 'APPROVED' AND verified_at IS NULL THEN updated_at
        ELSE verified_at
    END;

UPDATE users
SET role = 'ARTISAN'
WHERE id IN (SELECT user_id FROM artisans WHERE certification_status = 'APPROVED');
