-- Convert note audit columns from timestamptz to timestamp without time zone
-- so values are stored without +07 (or any offset suffix).
ALTER TABLE notes
    ALTER COLUMN created_at TYPE TIMESTAMP
        USING created_at AT TIME ZONE 'Asia/Bangkok',
    ALTER COLUMN updated_at TYPE TIMESTAMP
        USING updated_at AT TIME ZONE 'Asia/Bangkok';

