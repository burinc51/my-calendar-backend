package com.mycalendar.dev.payload.response;

import lombok.Builder;

@Builder
public record GroupInvitationSkipResponse(
        Long userId,
        String reason
) {
}

