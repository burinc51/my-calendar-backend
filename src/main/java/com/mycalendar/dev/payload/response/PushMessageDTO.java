package com.mycalendar.dev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO representing an Expo push message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushMessageDTO {
    private String to;           // Expo Push Token
    private String title;        // Notification title
    private String body;         // Notification body
    private Map<String, Object> data;  // Additional data
    private String sound;        // Sound (default)
    private String priority;     // Priority (high, normal)
    private String channelId;    // Android notification channel
}

