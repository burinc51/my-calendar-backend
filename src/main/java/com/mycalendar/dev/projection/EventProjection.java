package com.mycalendar.dev.projection;

import java.time.LocalDateTime;

public interface EventProjection {
    Long getEventId();

    String getTitle();

    String getDescription();

    LocalDateTime getStartDate();

    LocalDateTime getEndDate();

    String getLocation();

    Double getLatitude();

    Double getLongitude();

    LocalDateTime getNotificationTime();

    String getNotificationType();

    Integer getRemindBeforeMinutes();

    String getRepeatType();

    LocalDateTime getRepeatUntil();

    String getColor();

    String getCategory();

    String getPriority();

    Boolean getPinned();

    String getImageUrl();

    Long getCreateById();

    Long getGroupId();

    Long getUserId();

    String getUsername();

    String getName();
}
