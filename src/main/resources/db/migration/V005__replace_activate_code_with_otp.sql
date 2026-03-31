-- Replace legacy activate_code flow with OTP-based verification
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS otp_code VARCHAR(10);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS otp_expired_at TIMESTAMP;

ALTER TABLE users
    DROP COLUMN IF EXISTS activate_code;

