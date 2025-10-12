package com.mycalendar.dev.payload.response;

import lombok.Builder;

@Builder
public record GroupMemberResponse(
        Long userId,
        String name,
        String username,
        String role
) {
}
