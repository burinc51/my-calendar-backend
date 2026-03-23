package com.mycalendar.dev.payload.response.event;

import lombok.Builder;

@Builder
public record EventUserResponse(
        Long userId,
        String name,
        String username,
        String imageUrl
) {
}
