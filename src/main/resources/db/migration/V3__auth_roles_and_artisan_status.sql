ALTER TABLE users
    ADD COLUMN IF NOT EXISTS provider_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS provider VARCHAR(50),
    ADD COLUMN IF NOT EXISTS nickname VARCHAR(100),
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

UPDATE users
SET provider_id = COALESCE(provider_id, social_id),
    provider = COALESCE(provider, social_provider, 'supabase'),
    nickname = COALESCE(nickname, name, split_part(email, '@', 1))
WHERE provider_id IS NULL
   OR provider IS NULL
   OR nickname IS NULL;

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
SET name = COALESCE(name, intangible_heritage_name),
    heritage_category = COALESCE(heritage_category, intangible_heritage_type),
    certification_status = COALESCE(certification_status, verification_status, 'PENDING');

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
