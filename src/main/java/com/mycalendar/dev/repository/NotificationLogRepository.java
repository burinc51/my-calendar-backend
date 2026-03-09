package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for tracking sent notifications.
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /**
     * Checks whether a notification has already been sent for the given event, user, and type.
     * Used to prevent duplicate notifications.
     */
    boolean existsByEventIdAndUserIdAndNotificationType(Long eventId, Long userId, String notificationType);

    /** Get all logs for a specific event — shows who was notified and whether it succeeded. */
    List<NotificationLog> findByEventIdOrderBySentAtDesc(Long eventId);

    /** Get all push notification history for a specific user. */
    List<NotificationLog> findByUserIdOrderBySentAtDesc(Long userId);

    /** Get logs for an event filtered by status: SENT or FAILED. */
    List<NotificationLog> findByEventIdAndStatus(Long eventId, String status);
}
