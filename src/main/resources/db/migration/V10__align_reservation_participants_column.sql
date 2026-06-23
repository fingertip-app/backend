ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS participants INTEGER;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'reservations'
          AND column_name = 'number_of_participants'
    ) THEN
        EXECUTE 'UPDATE reservations SET participants = number_of_participants WHERE participants IS NULL';
    END IF;
END $$;

UPDATE reservations
SET participants = 1
WHERE participants IS NULL;

ALTER TABLE reservations
    ALTER COLUMN participants SET NOT NULL;
