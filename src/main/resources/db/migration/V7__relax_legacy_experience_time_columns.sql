DO $$
DECLARE
    column_name_to_relax text;
BEGIN
    FOREACH column_name_to_relax IN ARRAY ARRAY[
        'start_date_time',
        'end_date_time'
    ]
    LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'experiences'
              AND column_name = column_name_to_relax
        ) THEN
            EXECUTE format('ALTER TABLE experiences ALTER COLUMN %I DROP NOT NULL', column_name_to_relax);
        END IF;
    END LOOP;
END $$;
