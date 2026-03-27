CREATE TABLE IF NOT EXISTS notes (
    note_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(255),
    content TEXT,
    color VARCHAR(7),
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_date TIMESTAMPTZ,
    recurrence VARCHAR(20) NOT NULL DEFAULT 'none',
    start_date TIMESTAMPTZ,
    end_date TIMESTAMPTZ,
    location_name VARCHAR(255),
    location_link VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notes_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE SET NULL,
    CONSTRAINT chk_notes_recurrence
        CHECK (recurrence IN ('none', 'daily', 'weekly', 'monthly', 'yearly'))
);

CREATE TABLE IF NOT EXISTS note_tags (
    note_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    CONSTRAINT fk_note_tags_note
        FOREIGN KEY (note_id)
            REFERENCES notes (note_id)
            ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notes_user_id ON notes (user_id);
CREATE INDEX IF NOT EXISTS idx_notes_is_pinned ON notes (is_pinned);
CREATE INDEX IF NOT EXISTS idx_notes_updated_at ON notes (updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_note_tags_note_id ON note_tags (note_id);
CREATE INDEX IF NOT EXISTS idx_note_tags_tag ON note_tags (tag);

