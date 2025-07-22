package com.mycalendar.dev.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NoteResponse {
    private UUID noteId;
    private String userId;
    private String title;
    private String description;
    private String imageUrl;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    private String location;
    private boolean isPinned;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime notificationTime;
    private String repeating;
    private String color;
    private String category;
    private String priority;
}
