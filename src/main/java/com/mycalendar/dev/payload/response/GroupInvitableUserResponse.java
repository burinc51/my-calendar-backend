package com.mycalendar.dev.payload.response;

import lombok.Builder;

@Builder
public record GroupInvitableUserResponse(
        Long userId,
        String username,
        String name,
        String imageUrl,
        String inviteStatus
) {
}

