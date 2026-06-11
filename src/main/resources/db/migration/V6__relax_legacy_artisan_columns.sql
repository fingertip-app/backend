DO $$
DECLARE
    column_name_to_relax text;
BEGIN
    FOREACH column_name_to_relax IN ARRAY ARRAY[
        'intangible_heritage_name',
        'intangible_heritage_type',
        'verification_status'
    ]
    LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'artisans'
              AND column_name = column_name_to_relax
        ) THEN
            EXECUTE format('ALTER TABLE artisans ALTER COLUMN %I DROP NOT NULL', column_name_to_relax);
        END IF;
    END LOOP;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'artisans'
          AND column_name = 'intangible_heritage_name'
    ) THEN
        UPDATE artisans
        SET intangible_heritage_name = COALESCE(intangible_heritage_name, name);
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'artisans'
          AND column_name = 'intangible_heritage_type'
    ) THEN
        UPDATE artisans
        SET intangible_heritage_type = COALESCE(intangible_heritage_type, heritage_category);
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'artisans'
          AND column_name = 'verification_status'
    ) THEN
        UPDATE artisans
        SET verification_status = COALESCE(verification_status, certification_status);
    END IF;
END $$;
