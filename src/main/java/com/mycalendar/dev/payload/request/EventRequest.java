package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class EventRequest {
    private Long eventId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String location;
    private Double latitude;
    private Double longitude;

    // ===== Notification =====
    /** notificationTime is auto-calculated from remindBeforeValue+remindBeforeUnit if not provided explicitly */
    private LocalDateTime notificationTime;
    private String notificationType; // PUSH, EMAIL, POPUP
    /**
     * Numeric value of how far before the event to send the notification.
     * e.g. 15 (minutes), 2 (hours), 1 (day), 1 (week)
     */
    private Integer remindBeforeValue;
    /**
     * Unit for remindBeforeValue: MINUTES, HOURS, DAYS, WEEKS
     * Defaults to MINUTES when not specified.
     */
    private String remindBeforeUnit;
    /** Total minutes (auto-calculated from remindBeforeValue + remindBeforeUnit, kept for compatibility) */
    private Integer remindBeforeMinutes;

    // ===== Repeat =====
    /** Repeat type: NONE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM */
    private String repeatType;
    /** Date after which the event stops repeating */
    private LocalDateTime repeatUntil;
    /** Repeat every N units, e.g. 1 = every week, 2 = every 2 weeks */
    private Integer repeatInterval;
    /** Days of the week for WEEKLY/CUSTOM repeat, e.g. "MONDAY,WEDNESDAY,FRIDAY" */
    private String repeatDays;

    private String color;
    private String category;
    private String priority;
    private Boolean pinned;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Creator user ID is required")
    private Long createById;

    private Boolean allDay;
    private Set<Long> assigneeIds;
}


