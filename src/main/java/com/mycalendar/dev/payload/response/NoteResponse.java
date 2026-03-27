package com.mycalendar.dev.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
