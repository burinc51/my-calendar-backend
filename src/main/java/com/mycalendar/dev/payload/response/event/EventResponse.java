package com.mycalendar.dev.payload.response.event;

import lombok.Builder;

import java.util.List;

@Builder
public record EventResponse(
        Long eventId,
        String title,
        String description,
        String startDate,
        String endDate,

        String location,
        Double latitude,
        Double longitude,

        String notificationTime,
        String notificationType,
        Integer remindBeforeMinutes,

        String repeatType,
        String repeatUntil,

        String color,
        String category,
        String priority,
        Boolean pinned,

        String imageUrl,

        Long createById,
        Long groupId,
        List<EventUserResponse> assignees
) {
}
