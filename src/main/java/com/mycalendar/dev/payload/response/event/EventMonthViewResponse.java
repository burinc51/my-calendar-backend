package com.mycalendar.dev.payload.response.event;

import lombok.Builder;

import java.util.List;

@Builder
public record EventMonthViewResponse(
        Long eventId,
        String title,
        String startDate,
        String endDate,
        String color,
        Boolean allDay,
        String priority,
        List<EventUserSummaryResponse> assignees,
        EventUserSummaryResponse createdBy
) {
}
