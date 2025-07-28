package com.mycalendar.dev.payload.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    private Long eventId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private boolean isPinned = false;
    private LocalDateTime notificationTime;
    private String repeating;
    private String color;
    private String category;
    private String priority;
}
