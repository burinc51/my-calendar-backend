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
