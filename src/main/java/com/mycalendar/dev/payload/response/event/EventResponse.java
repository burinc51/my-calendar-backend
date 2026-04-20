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
        Integer remindBeforeValue,
        String remindBeforeUnit,
        Integer remindBeforeMinutes,

        String repeatType,
        String repeatUntil,
        Integer repeatInterval,
        String repeatDays,

        String color,
        String category,
        String priority,
        Boolean pinned,
        Boolean allDay,
        String imageUrl,
        Long createById,
        Long groupId,
        String groupName,
        String icon,
        String groupColor,
        String bg,
        List<EventUserResponse> assignees
) {
}


