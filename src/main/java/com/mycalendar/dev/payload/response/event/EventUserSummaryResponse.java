package com.mycalendar.dev.payload.response.event;

import lombok.Builder;

@Builder
public record EventUserSummaryResponse(
        Long userId,
        String username,
        String name,
        String imageUrl
) {
}

