package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.entity.NotificationLog;
import com.mycalendar.dev.payload.response.NotificationLogResponse;
import com.mycalendar.dev.repository.NotificationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification-logs")
@RequiredArgsConstructor
@Tag(name = "Notification Logs", description = "Check whether push notifications were sent for events")
public class NotificationLogController {

    private final NotificationLogRepository notificationLogRepository;

    /**
     * GET /api/v1/notification-logs/event/{eventId}
     *
     * Check all push notifications sent for a specific event.
     * Use this after creating an event to verify that the Expo push
     * was delivered when notificationTime was reached.
     *
     * Response includes:
     *  - userId  : who received the push
     *  - status  : SENT or FAILED
     *  - sentAt  : timestamp when it was attempted
     */
    @GetMapping("/event/{eventId}")
    @Operation(
            summary = "Get notification logs for an event",
            description = "Returns every push notification attempt for the given event. " +
                          "status=SENT means Expo accepted it; status=FAILED means something went wrong."
    )
    public ResponseEntity<List<NotificationLogResponse>> getByEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) String status) {

        List<NotificationLog> logs = (status != null)
                ? notificationLogRepository.findByEventIdAndStatus(eventId, status.toUpperCase())
                : notificationLogRepository.findByEventIdOrderBySentAtDesc(eventId);

        return ResponseEntity.ok(logs.stream().map(this::toResponse).toList());
    }

    /**
     * GET /api/v1/notification-logs/user/{userId}
     *
     * Get the full push notification history for a user
     * (all events they were notified about).
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get notification history for a user",
            description = "Returns all push notification attempts sent to a specific user, newest first."
    )
    public ResponseEntity<List<NotificationLogResponse>> getByUser(@PathVariable Long userId) {
        List<NotificationLog> logs = notificationLogRepository.findByUserIdOrderBySentAtDesc(userId);
        return ResponseEntity.ok(logs.stream().map(this::toResponse).toList());
    }

    // mapper
    private NotificationLogResponse toResponse(NotificationLog log) {
        return NotificationLogResponse.builder()
                .id(log.getId())
                .eventId(log.getEventId())
                .userId(log.getUserId())
                .notificationType(log.getNotificationType())
                .status(log.getStatus())
                .errorMessage(log.getErrorMessage())
                .sentAt(log.getSentAt())
                .build();
    }
}

