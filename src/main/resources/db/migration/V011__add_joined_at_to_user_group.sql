ALTER TABLE user_group
ADD COLUMN joined_at TIMESTAMP NULL;

CREATE INDEX idx_user_group_user_group_joined_at
ON user_group(user_id, group_id, joined_at);

