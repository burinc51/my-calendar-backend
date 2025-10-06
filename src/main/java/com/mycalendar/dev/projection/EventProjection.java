package com.mycalendar.dev.projection;

import java.time.LocalDateTime;

public interface EventProjection {
    Long getEventId();

    Long getUserId();

    String getTitle();

    String getDescription();

    String getImageUrl();

    LocalDateTime getStartDate();

    LocalDateTime getEndDate();

    String getLocation();

    boolean getIsPinned();

    LocalDateTime getNotificationTime();

    String getRepeating();

    String getColor();

    String getCategory();

    String getPriority();

    Long getGroupId();
}
