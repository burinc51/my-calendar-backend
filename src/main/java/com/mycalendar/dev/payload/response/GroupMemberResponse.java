package com.mycalendar.dev.payload.response;

import lombok.Builder;

@Builder
public record GroupMemberResponse(
        Long userId,
        String initialText,
        String avatarColor,
        String pictureUrl
) {
}
