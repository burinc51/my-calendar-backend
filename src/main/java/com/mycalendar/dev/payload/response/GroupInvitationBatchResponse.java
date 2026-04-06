package com.mycalendar.dev.payload.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GroupInvitationBatchResponse(
        Long groupId,
        Integer invitedCount,
        Integer skippedCount,
        List<GroupInvitationResponse> invitations,
        List<GroupInvitationSkipResponse> skipped
) {
}

