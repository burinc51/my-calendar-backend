package com.mycalendar.dev.payload.response.event;

import lombok.Builder;

import java.util.Map;
import java.util.List;

@Builder
public record EventYearSummaryResponse(
        Integer year,
        Map<Integer, List<Integer>> months
) {
}

