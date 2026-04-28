package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.entity.NotificationLog;
import com.mycalendar.dev.entity.PushToken;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.mapper.EventMapper;
import com.mycalendar.dev.payload.request.PushTokenRequest;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.NotificationLogRepository;
import com.mycalendar.dev.repository.PushTokenRepository;
import com.mycalendar.dev.service.ExpoPushService;
import com.mycalendar.dev.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Bangkok");
    
    private final EventRepository eventRepository;
    private final PushTokenRepository pushTokenRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final ExpoPushService expoPushService;
    
    /**
     * Process events that are due for notifications.
     * Called by the Quartz Scheduler every 1 minute.
     */
    @Override
    @Transactional
    public void processEventNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(2); // Time window: last 2 minutes
        log.debug("🔔 Processing event notifications at: {} (window: {} to {})", now, windowStart, now);
        
        // Find events due for notification (optimized query)
        List<Event> eventsToNotify = eventRepository.findEventsToNotify(now, windowStart);
        
        if (eventsToNotify.isEmpty()) {
            log.debug("No events to notify at this time");
            return;
        }
        
        log.info("📋 Found {} events to notify", eventsToNotify.size());

        List<Event> processedEvents = new java.util.ArrayList<>();

        for (Event event : eventsToNotify) {
            if (!eventRepository.existsById(event.getEventId())) {
                log.info("⏭️ Skip notification for deleted event {}", event.getEventId());
                continue;
            }

            processEventNotification(event);

            // Mark as notified only if event still exists after processing.
            if (eventRepository.existsById(event.getEventId())) {
                event.setNotificationSent(true);
                processedEvents.add(event);
            }
        }

        // Batch save only events that were still present at update time.
        if (!processedEvents.isEmpty()) {
            eventRepository.saveAll(processedEvents);
        }
    }
    
    /**
     * Process notification for a single event.
     * Always sends a PUSH notification — only PUSH is supported.
     */
    private void processEventNotification(Event event) {
        Set<User> users = event.getUsers();

        if (users == null || users.isEmpty()) {
            log.warn("Event {} has no assigned users, skipping notification", event.getEventId());
            return;
        }

        for (User user : users) {
            // Skip if already sent to this user for this event
            if (notificationLogRepository.existsByEventIdAndUserIdAndNotificationType(
                    event.getEventId(), user.getUserId(), "PUSH")) {
                log.debug("Push notification already sent for event {} to user {}",
                        event.getEventId(), user.getUserId());
                continue;
            }

            boolean success = sendPushNotification(event, user);

            NotificationLog logEntry = new NotificationLog(
                    event.getEventId(),
                    user.getUserId(),
                    "PUSH",
                    success ? "SENT" : "FAILED"
            );
            notificationLogRepository.save(logEntry);
        }
    }
    
    /**
     * Send push notification to a user
     */
    private boolean sendPushNotification(Event event, User user) {
        // Find user's active push tokens
        List<PushToken> tokens = pushTokenRepository.findByUserIdAndActiveTrue(user.getUserId());
        
        if (tokens.isEmpty()) {
            log.warn("No active push tokens for user {}", user.getUserId());
            return false;
        }
        
        // Build notification message
        String title = "🔔 " + event.getTitle();
        String body = buildReminderBody(event);

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            body += " • " + event.getLocation();
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", event.getEventId());
        data.put("type", "event_reminder");
        
        boolean anySuccess = false;
        
        for (PushToken token : tokens) {
            boolean success = expoPushService.sendPushNotification(
                    token.getToken(), title, body, data);
            if (success) {
                anySuccess = true;
            }
        }
        
        return anySuccess;
    }
    
    /**
     * Builds a human-friendly reminder body string.
     * Uses remindBeforeValue + remindBeforeUnit when available,
     * falls back to remindBeforeMinutes, then defaults to 15 minutes.
     */
    private String buildReminderBody(Event event) {
        Integer value = event.getRemindBeforeValue();
        String unit  = event.getRemindBeforeUnit();

        if (value != null && unit != null) {
            String unitLabel = switch (unit.toUpperCase()) {
                case "HOURS" -> value == 1 ? "hour"   : "hours";
                case "DAYS"  -> value == 1 ? "day"    : "days";
                case "WEEKS" -> value == 1 ? "week"   : "weeks";
                default      -> value == 1 ? "minute" : "minutes";
            };
            return String.format("Event starts in %d %s", value, unitLabel);
        }

        // Fallback: use remindBeforeMinutes
        int minutes = event.getRemindBeforeMinutes() != null ? event.getRemindBeforeMinutes() : 15;
        return String.format("Event starts in %d %s", minutes, minutes == 1 ? "minute" : "minutes");
    }

    /**
     * Advances recurring events to the next occurrence.
     * Called after processEventNotifications() on every scheduler tick.
     */
    @Override
    @Transactional
    public void rescheduleRecurringNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> recurringEvents = eventRepository.findRecurringEventsToReschedule(now);

        if (recurringEvents.isEmpty()) {
            log.debug("No recurring events to reschedule");
            return;
        }

        log.info("🔁 Rescheduling {} recurring event(s)", recurringEvents.size());

        for (Event event : recurringEvents) {
            LocalDateTime nextStart = computeNextOccurrence(event, now);
            if (nextStart == null) {
                // No more occurrences (past repeatUntil)
                log.info("✅ Recurring event {} has no more occurrences, stopping repeat", event.getEventId());
                event.setRepeatType("NONE");
                continue;
            }

            // Calculate event duration to shift endDate accordingly
            long durationMinutes = 0;
            if (event.getEndDate() != null && event.getStartDate() != null) {
                durationMinutes = java.time.Duration.between(event.getStartDate(), event.getEndDate()).toMinutes();
            }

            event.setStartDate(nextStart);
            if (durationMinutes > 0) {
                event.setEndDate(nextStart.plusMinutes(durationMinutes));
            }

            // Recalculate notificationTime from remindBeforeMinutes
            if (event.getRemindBeforeMinutes() != null) {
                event.setNotificationTime(nextStart.minusMinutes(event.getRemindBeforeMinutes()));
            } else if (event.getNotificationTime() != null) {
                // Shift notificationTime by the same offset as before
                long offsetMinutes = java.time.Duration.between(event.getNotificationTime(), event.getStartDate()).toMinutes();
                event.setNotificationTime(nextStart.minusMinutes(offsetMinutes));
            }

            // Reset flag so the next occurrence triggers a notification
            event.setNotificationSent(false);

            log.info("🔁 Event {} rescheduled → startDate: {}, notificationTime: {}",
                    event.getEventId(), event.getStartDate(), event.getNotificationTime());
        }

        eventRepository.saveAll(recurringEvents);
    }

    /**
     * Computes the next occurrence date/time of a recurring event.
     */
    private LocalDateTime computeNextOccurrence(Event event, LocalDateTime now) {
        String repeatType = event.getRepeatType();
        int interval = event.getRepeatInterval() != null ? event.getRepeatInterval() : 1;
        LocalDateTime current = event.getStartDate();

        LocalDateTime next = switch (repeatType) {
            case "DAILY"   -> current.plusDays(interval);
            case "WEEKLY"  -> computeNextWeeklyOccurrence(event, current, interval, now);
            case "MONTHLY" -> current.plusMonths(interval);
            case "YEARLY"  -> current.plusYears(interval);
            case "CUSTOM"  -> computeNextWeeklyOccurrence(event, current, interval, now);
            default        -> null;
        };

        if (next == null) return null;

        // Stop if the next occurrence is past repeatUntil
        if (event.getRepeatUntil() != null && next.isAfter(event.getRepeatUntil())) {
            return null;
        }

        return next;
    }

    /**
     * Computes the next occurrence for WEEKLY/CUSTOM repeat types,
     * respecting the repeatDays constraint.
     */
    private LocalDateTime computeNextWeeklyOccurrence(Event event, LocalDateTime current, int interval, LocalDateTime now) {
        String repeatDays = event.getRepeatDays();

        // No repeatDays defined — simply advance by N weeks
        if (repeatDays == null || repeatDays.isBlank()) {
            return current.plusWeeks(interval);
        }

        // Parse repeatDays into a Set<DayOfWeek>
        Set<DayOfWeek> allowedDays = new java.util.HashSet<>();
        for (String day : repeatDays.split(",")) {
            try {
                allowedDays.add(DayOfWeek.valueOf(day.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                log.warn("Invalid repeatDays value: {}", day);
            }
        }

        if (allowedDays.isEmpty()) {
            return current.plusWeeks(interval);
        }

        // Search the next allowed day within the next 14 days
        LocalDateTime candidate = current.plusDays(1);
        for (int i = 0; i < 14; i++) {
            if (allowedDays.contains(candidate.getDayOfWeek()) && candidate.isAfter(now)) {
                return candidate;
            }
            candidate = candidate.plusDays(1);
        }

        // Fallback: advance by N weeks
        return current.plusWeeks(interval);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getNotificationScheduleByDate(LocalDate date, boolean includeSent, Long groupId) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now(APP_ZONE);
        LocalDateTime fromDateTime = targetDate.atStartOfDay();
        LocalDateTime toDateTime = targetDate.plusDays(1).atStartOfDay();

        return eventRepository.findNotificationScheduleByDate(fromDateTime, toDateTime, includeSent, groupId)
                .stream()
                .map(EventMapper::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Register Push Token
     */
    @Override
    @Transactional
    public void registerPushToken(PushTokenRequest request) {
        // Check if token already exists
        if (pushTokenRepository.existsByToken(request.getToken())) {
            log.info("Push token already registered: {}", maskToken(request.getToken()));
            return;
        }
        
        PushToken pushToken = new PushToken();
        pushToken.setToken(request.getToken());
        pushToken.setUserId(request.getUserId());
        pushToken.setDeviceName(request.getDeviceName());
        pushToken.setActive(true);
        
        pushTokenRepository.save(pushToken);
        log.info("✅ Push token registered for user {}: {}", 
                request.getUserId(), maskToken(request.getToken()));
    }
    
    /**
     * Unregister Push Token
     */
    @Override
    @Transactional
    public void unregisterPushToken(String token) {
        pushTokenRepository.deleteByToken(token);
        log.info("🗑️ Push token unregistered: {}", maskToken(token));
    }

    @Override
    public boolean sendTestPushNotification(String token, String title, String body, Map<String, Object> data) {
        Map<String, Object> payload = new HashMap<>();
        if (data != null) {
            payload.putAll(data);
        }
        payload.putIfAbsent("type", "manual_test");
        payload.putIfAbsent("sentAt", LocalDateTime.now().toString());

        boolean success = expoPushService.sendPushNotification(token, title, body, payload);
        if (success) {
            log.info("✅ Test push notification sent to token: {}", maskToken(token));
        } else {
            log.warn("❌ Failed to send test push notification to token: {}", maskToken(token));
        }
        return success;
    }
    
    /**
     * Mask part of the token for logging
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, Math.min(20, token.length())) + "...";
    }
}
