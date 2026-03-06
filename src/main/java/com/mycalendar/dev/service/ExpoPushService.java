package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.response.PushMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Expo Push API Documentation: https://docs.expo.dev/push-notifications/sending-notifications/
 */
@Service
@Slf4j
public class ExpoPushService {
    
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    
    private final RestTemplate restTemplate;
    
    public ExpoPushService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Send a push notification to a single device via Expo Push service.
     *
     * @param token Expo Push Token (ExponentPushToken[xxx])
     * @param title Notification title
     * @param body Notification body
     * @param data Additional data to include with the notification
     * @return true if the message was sent successfully
     */
    public boolean sendPushNotification(String token, String title, String body, Map<String, Object> data) {
        try {
            if (!isValidExpoPushToken(token)) {
                log.warn("Invalid Expo Push Token: {}", token);
                return false;
            }
            
            PushMessageDTO message = PushMessageDTO.builder()
                    .to(token)
                    .title(title)
                    .body(body)
                    .data(data != null ? data : new HashMap<>())
                    .sound("default")
                    .priority("high")
                    .channelId("default")
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            
            HttpEntity<PushMessageDTO> request = new HttpEntity<>(message, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    EXPO_PUSH_URL,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Push notification sent successfully to token: {}", maskToken(token));
                return true;
            } else {
                log.error("Failed to send push notification: {}", response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending push notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send push notifications in batch (multiple devices at once)
     *
     * @param messages List of messages to send
     * @return Number of messages attempted (or 0 on failure)
     */
    public int sendBatchNotifications(List<PushMessageDTO> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            
            HttpEntity<List<PushMessageDTO>> request = new HttpEntity<>(messages, headers);
            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    EXPO_PUSH_URL,
                    HttpMethod.POST,
                    request,
                    (Class<List<Map<String, Object>>>) (Class) List.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Batch push notifications sent: {} messages", messages.size());
                return messages.size();
            }
            return 0;
        } catch (Exception e) {
            log.error("Error sending batch push notifications: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    public boolean isValidExpoPushToken(String token) {
        return token != null && 
               (token.startsWith("ExponentPushToken[") || token.startsWith("ExpoPushToken["));
    }
    
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 20) + "..." + token.substring(token.length() - 5);
    }
}
