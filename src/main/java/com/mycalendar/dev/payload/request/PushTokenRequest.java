package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO สำหรับ register Push Token
 */
@Data
public class PushTokenRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String deviceName;
}
