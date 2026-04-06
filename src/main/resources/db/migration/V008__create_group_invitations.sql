CREATE TABLE IF NOT EXISTS group_invitations (
    invitation_id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    invited_user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    invited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    CONSTRAINT fk_group_invitations_group FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE,
    CONSTRAINT fk_group_invitations_inviter FOREIGN KEY (inviter_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_group_invitations_invited FOREIGN KEY (invited_user_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_group_invitations_group_id ON group_invitations(group_id);
CREATE INDEX IF NOT EXISTS idx_group_invitations_invited_user_id ON group_invitations(invited_user_id);
CREATE INDEX IF NOT EXISTS idx_group_invitations_status ON group_invitations(status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_group_invitations_pending
    ON group_invitations(group_id, invited_user_id)
    WHERE status = 'PENDING';

