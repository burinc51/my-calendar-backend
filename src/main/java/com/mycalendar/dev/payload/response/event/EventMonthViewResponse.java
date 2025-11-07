package com.mycalendar.dev.payload.response.event;

import lombok.Builder;

@Builder
public record EventMonthViewResponse(
        Long eventId,
        String title,
        String startDate,
        String endDate,
        String color,
        Boolean allDay
) {
}
