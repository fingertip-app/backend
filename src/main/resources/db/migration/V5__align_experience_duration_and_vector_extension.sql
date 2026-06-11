CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE experiences
    ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;

UPDATE experiences
SET duration_minutes = 60
WHERE duration_minutes IS NULL;

ALTER TABLE experiences
    ALTER COLUMN duration_minutes SET DEFAULT 60,
    ALTER COLUMN duration_minutes SET NOT NULL;
