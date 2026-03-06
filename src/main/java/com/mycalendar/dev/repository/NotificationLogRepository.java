package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for tracking sent notifications.
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Checks whether a notification has already been sent for the given event, user, and type.
     * Used to prevent duplicate notifications.
     */
    boolean existsByEventIdAndUserIdAndNotificationType(Long eventId, Long userId, String notificationType);
}
