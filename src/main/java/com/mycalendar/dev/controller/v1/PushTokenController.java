package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.PushTokenRequest;
import com.mycalendar.dev.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/push-tokens")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Push Token")
public class PushTokenController {
    
    private final INotificationService notificationService;
    
    /**
     * Register a Push Token
     * Called from the React Native app after getting an Expo Push Token
     */
    @PostMapping
    @Operation(summary = "Register push token", description = "Register Expo Push Token for the user")
    public ResponseEntity<Map<String, Object>> registerToken(@Valid @RequestBody PushTokenRequest request) {
        log.info("📱 Registering push token for user: {}", request.getUserId());
        
        notificationService.registerPushToken(request);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Push token registered successfully"
        ));
    }
    
    /**
     * Unregister a Push Token
     * Called when the user logs out
     */
    @DeleteMapping
    @Operation(summary = "Unregister push token", description = "Unregister the Push Token (on logout)")
    public ResponseEntity<Map<String, Object>> unregisterToken(
            @RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token is required"
            ));
        }
        
        log.info("🗑️ Unregistering push token");
        
        notificationService.unregisterPushToken(token);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Push token unregistered successfully"
        ));
    }
    
    /**
     * Test endpoint for verifying the Notification Job
     * (for development use only)
     */
    @PostMapping("/test-job")
    @Operation(summary = "Test notification job", description = "Trigger notification job for testing (dev only)")
    public ResponseEntity<Map<String, Object>> testNotificationJob() {
        log.info("🧪 Manually triggering notification job for testing");
        
        notificationService.processEventNotifications();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification job executed"
        ));
    }
}
