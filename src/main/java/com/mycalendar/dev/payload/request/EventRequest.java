package com.mycalendar.dev.payload.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class EventRequest {
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String location;
    private Double latitude;
    private Double longitude;

    private LocalDateTime notificationTime;
    private String notificationType;
    private Integer remindBeforeMinutes;

    private String repeatType;
    private LocalDateTime repeatUntil;

    private String color;
    private String category;
    private String priority;
    private Boolean pinned;

    private Long groupId;
    private Long createById;
    private Set<Long> assigneeIds;
}
