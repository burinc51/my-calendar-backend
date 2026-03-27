package com.mycalendar.dev.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class NoteResponse {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String color;
    private Boolean isPinned;
    private List<String> tags;
    private OffsetDateTime reminderDate;
    private String recurrence;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String locationName;
    private String locationLink;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

