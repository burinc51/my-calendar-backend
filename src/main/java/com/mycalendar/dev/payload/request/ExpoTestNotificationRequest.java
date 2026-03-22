package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ExpoTestNotificationRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private Map<String, Object> data;
}
