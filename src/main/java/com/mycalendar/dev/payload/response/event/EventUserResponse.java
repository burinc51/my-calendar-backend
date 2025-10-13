package com.mycalendar.dev.payload.response.event;

import lombok.Builder;

@Builder
public record EventUserResponse(
        Long userId,
        String username,
        String name
) {
}
