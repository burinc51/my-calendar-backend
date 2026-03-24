package com.mycalendar.dev.payload.response;

import lombok.Builder;

@Builder
public record GroupUserResponse(
        Long userId,
        String username,
        String name,
        String imageUrl
) {
}

