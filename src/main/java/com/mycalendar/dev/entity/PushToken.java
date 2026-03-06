package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for storing a user's Expo push token.
 * Used to send push notifications to the user's device.
 */
@Entity
@Table(name = "push_tokens", indexes = {
        @Index(name = "idx_push_token_user_id", columnList = "userId"),
        @Index(name = "idx_push_token_token", columnList = "token")
})
@Getter
@Setter
@NoArgsConstructor
public class PushToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token; // ExponentPushToken[xxxxxxx]
    
    @Column(nullable = false)
    private Long userId;
    
    private String deviceName;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
