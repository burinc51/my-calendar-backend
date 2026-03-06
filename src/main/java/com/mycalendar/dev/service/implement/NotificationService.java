package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.entity.NotificationLog;
import com.mycalendar.dev.entity.PushToken;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.payload.request.PushTokenRequest;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {
    
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
        
        for (Event event : eventsToNotify) {
            processEventNotification(event);
            
            // Mark event as notified
            event.setNotificationSent(true);
        }
        
        // Batch save all processed events
        eventRepository.saveAll(eventsToNotify);
    }
    
    /**
     * Process notification for a single event
     */
    private void processEventNotification(Event event) {
        String notificationType = event.getNotificationType();
        Set<User> users = event.getUsers();
        
        if (users == null || users.isEmpty()) {
            log.warn("Event {} has no assigned users, skipping notification", event.getEventId());
            return;
        }
        
        for (User user : users) {
            // Check if notification has already been sent
            if (notificationLogRepository.existsByEventIdAndUserIdAndNotificationType(
                    event.getEventId(), user.getUserId(), notificationType)) {
                log.debug("Notification already sent for event {} to user {}", 
                        event.getEventId(), user.getUserId());
                continue;
            }
            
            boolean success = false;
            
            if ("PUSH".equals(notificationType)) {
                success = sendPushNotification(event, user);
            } else if ("EMAIL".equals(notificationType)) {
                // TODO: Implement email notification
                log.info("📧 Email notification for event {} to user {} (not implemented yet)", 
                        event.getEventId(), user.getUserId());
                success = true; // Mark as success for now
            }
            
            NotificationLog logEntry = new NotificationLog(
                    event.getEventId(),
                    user.getUserId(),
                    notificationType,
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
        String body = String.format("Event starts in %d minutes",
                event.getRemindBeforeMinutes() != null ? event.getRemindBeforeMinutes() : 15);
        
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
     * เลื่อน recurring events ไปยัง occurrence ถัดไป
     * เรียกหลัง processEventNotifications() ทุกครั้ง
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
                // ไม่มี occurrence ถัดไปแล้ว (เกิน repeatUntil)
                log.info("✅ Recurring event {} has no more occurrences, stopping repeat", event.getEventId());
                event.setRepeatType("NONE");
                continue;
            }

            // คำนวณ duration ของ event เพื่อเลื่อน endDate ไปด้วย
            long durationMinutes = 0;
            if (event.getEndDate() != null && event.getStartDate() != null) {
                durationMinutes = java.time.Duration.between(event.getStartDate(), event.getEndDate()).toMinutes();
            }

            event.setStartDate(nextStart);
            if (durationMinutes > 0) {
                event.setEndDate(nextStart.plusMinutes(durationMinutes));
            }

            // คำนวณ notificationTime ใหม่จาก remindBeforeMinutes
            if (event.getRemindBeforeMinutes() != null) {
                event.setNotificationTime(nextStart.minusMinutes(event.getRemindBeforeMinutes()));
            } else if (event.getNotificationTime() != null) {
                // เลื่อน notificationTime ตาม offset เดิม
                long offsetMinutes = java.time.Duration.between(event.getNotificationTime(), event.getStartDate()).toMinutes();
                event.setNotificationTime(nextStart.minusMinutes(offsetMinutes));
            }

            // Reset flag เพื่อให้แจ้งเตือน occurrence ถัดไป
            event.setNotificationSent(false);

            log.info("🔁 Event {} rescheduled → startDate: {}, notificationTime: {}",
                    event.getEventId(), event.getStartDate(), event.getNotificationTime());
        }

        eventRepository.saveAll(recurringEvents);
    }

    /**
     * คำนวณ occurrence ถัดไปของ recurring event
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

        // ถ้าเกิน repeatUntil ให้หยุด
        if (event.getRepeatUntil() != null && next.isAfter(event.getRepeatUntil())) {
            return null;
        }

        return next;
    }

    /**
     * คำนวณ next occurrence สำหรับ WEEKLY/CUSTOM โดยพิจารณา repeatDays
     */
    private LocalDateTime computeNextWeeklyOccurrence(Event event, LocalDateTime current, int interval, LocalDateTime now) {
        String repeatDays = event.getRepeatDays();

        // ถ้าไม่มี repeatDays ให้เลื่อนไปอีก N สัปดาห์ตรงๆ
        if (repeatDays == null || repeatDays.isBlank()) {
            return current.plusWeeks(interval);
        }

        // แปลง repeatDays เป็น Set<DayOfWeek>
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

        // หาวันถัดไปในสัปดาห์เดียวกัน หรือข้ามสัปดาห์
        LocalDateTime candidate = current.plusDays(1);
        for (int i = 0; i < 14; i++) { // ค้นหาสูงสุด 14 วัน
            if (allowedDays.contains(candidate.getDayOfWeek()) && candidate.isAfter(now)) {
                return candidate;
            }
            candidate = candidate.plusDays(1);
        }

        // fallback: เลื่อนไปอีก N สัปดาห์
        return current.plusWeeks(interval);
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
