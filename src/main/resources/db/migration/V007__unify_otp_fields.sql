-- Unify OTP fields for activation and forgot-password flows
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS otp_purpose VARCHAR(30);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS otp_verified_at TIMESTAMP;

-- Backfill from legacy forgot-password OTP columns when present
UPDATE users
SET otp_code = forgot_password_otp_code,
    otp_expired_at = forgot_password_otp_expired_at,
    otp_purpose = 'FORGOT_PASSWORD',
    otp_verified_at = forgot_password_verified_at
WHERE forgot_password_otp_code IS NOT NULL;

ALTER TABLE users
    DROP COLUMN IF EXISTS forgot_password_otp_code;

ALTER TABLE users
    DROP COLUMN IF EXISTS forgot_password_otp_expired_at;

ALTER TABLE users
    DROP COLUMN IF EXISTS forgot_password_verified_at;

