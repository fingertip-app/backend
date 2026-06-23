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
        EXECUTE 'UPDATE reservations SET number_of_participants = participants WHERE number_of_participants IS NULL';
        EXECUTE 'ALTER TABLE reservations ALTER COLUMN number_of_participants DROP NOT NULL';
    END IF;
END $$;
