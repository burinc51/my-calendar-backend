package com.mycalendar.dev.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mycalendar.dev.enums.GroupInvitationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GroupInvitationResponse(
        Long invitationId,
        Long groupId,
        String groupName,
        Long inviterUserId,
        String inviterName,
        Long invitedUserId,
        String invitedUserName,
        GroupInvitationStatus status,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime invitedAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime respondedAt
) {
}

