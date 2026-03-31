-- Forgot password OTP flow
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS forgot_password_otp_code VARCHAR(10);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS forgot_password_otp_expired_at TIMESTAMP;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS forgot_password_verified_at TIMESTAMP;

