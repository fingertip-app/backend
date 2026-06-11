ALTER TABLE experiences
    ADD COLUMN IF NOT EXISTS category VARCHAR(100);

UPDATE experiences
SET category = 'etc'
WHERE category IS NULL;

ALTER TABLE experiences
    ALTER COLUMN category SET DEFAULT 'etc',
    ALTER COLUMN category SET NOT NULL;

CREATE TABLE IF NOT EXISTS experience_schedules (
    id BIGSERIAL PRIMARY KEY,
    available_slots INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    scheduled_at TIMESTAMP NOT NULL,
    experience_id BIGINT NOT NULL REFERENCES experiences(id)
);

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS schedule_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_reservations_schedule'
    ) THEN
        ALTER TABLE reservations
            ADD CONSTRAINT fk_reservations_schedule
            FOREIGN KEY (schedule_id)
            REFERENCES experience_schedules(id)
            NOT VALID;
    END IF;
END $$;
