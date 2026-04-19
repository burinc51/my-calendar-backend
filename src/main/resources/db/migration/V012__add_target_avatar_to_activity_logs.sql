-- Add target_avatar column to activity_logs table
ALTER TABLE activity_logs
    ADD COLUMN target_avatar VARCHAR(1000);

COMMENT ON COLUMN activity_logs.target_avatar IS 'Avatar / profile-picture URL of the target user at the time of action (snapshot)';

