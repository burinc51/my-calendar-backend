package com.mycalendar.dev.payload.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for a notification log entry.
 * Tells the caller whether a push notification was successfully sent
 * for a given event/user combination.
 */
@Builder
public record NotificationLogResponse(
        Long id,
        Long eventId,
        Long userId,
        String notificationType,   // always "PUSH"
        String status,             // "SENT" or "FAILED"
        String errorMessage,       // non-null only when status = FAILED
        LocalDateTime sentAt
) {}

