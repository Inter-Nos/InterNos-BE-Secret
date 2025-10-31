-- Create enums
CREATE TYPE content_type AS ENUM ('TEXT', 'IMAGE');
CREATE TYPE visibility_type AS ENUM ('PUBLIC', 'PRIVATE');
CREATE TYPE policy_type AS ENUM ('ONCE', 'LIMITED', 'UNLIMITED');

-- SECRET_ROOM table
CREATE TABLE secret_room (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    owner_name TEXT NOT NULL,
    title TEXT NOT NULL,
    hint TEXT NOT NULL,
    answer_hash TEXT NOT NULL,
    content_type content_type NOT NULL,
    content_text TEXT,
    image_ref TEXT,
    image_meta JSONB,
    alt TEXT,
    visibility visibility_type NOT NULL,
    policy policy_type NOT NULL,
    view_limit INTEGER,
    views_used INTEGER NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT check_content_text CHECK (
        (content_type = 'TEXT' AND content_text IS NOT NULL) OR
        (content_type = 'IMAGE' AND content_text IS NULL)
    ),
    CONSTRAINT check_image_ref CHECK (
        (content_type = 'IMAGE' AND image_ref IS NOT NULL) OR
        (content_type = 'TEXT' AND image_ref IS NULL)
    ),
    CONSTRAINT check_view_limit CHECK (
        (policy = 'LIMITED' AND view_limit IS NOT NULL AND view_limit >= 1) OR
        (policy != 'LIMITED')
    )
);

-- ATTEMPT table
CREATE TABLE attempt (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    solver_anon_id TEXT,
    is_correct BOOLEAN NOT NULL,
    latency_ms INTEGER,
    ip_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- VISIT_LOG_ROOM table
CREATE TABLE visit_log_room (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    visitor_anon_id TEXT,
    ip_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- LOCKOUT table
CREATE TABLE lockout (
    room_id BIGINT NOT NULL,
    ip_hash TEXT NOT NULL,
    until TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (room_id, ip_hash)
);

-- Indexes for SECRET_ROOM
CREATE INDEX idx_secret_room_visibility_active_created ON secret_room(visibility, is_active, created_at DESC);
CREATE INDEX idx_secret_room_owner_created ON secret_room(owner_id, created_at DESC);

-- Indexes for ATTEMPT
CREATE INDEX idx_attempt_room_created ON attempt(room_id, created_at DESC);
CREATE INDEX idx_attempt_room_correct_created ON attempt(room_id, is_correct, created_at DESC);

-- Indexes for VISIT_LOG_ROOM
CREATE INDEX idx_visit_log_room_room_created ON visit_log_room(room_id, created_at DESC);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_secret_room_updated_at
    BEFORE UPDATE ON secret_room
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

