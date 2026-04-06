ALTER TABLE activity_logs
ADD COLUMN IF NOT EXISTS invitation_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_activity_log_invitation
    ON activity_logs(invitation_id);

ALTER TABLE activity_logs
ADD CONSTRAINT fk_activity_logs_invitation
    FOREIGN KEY (invitation_id)
    REFERENCES group_invitations(invitation_id)
    ON DELETE SET NULL;

