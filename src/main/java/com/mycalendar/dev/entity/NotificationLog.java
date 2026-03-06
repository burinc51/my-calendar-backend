package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notification_log_event_user", columnList = "eventId, userId, notificationType")
})
@Getter
@Setter
@NoArgsConstructor
public class NotificationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long eventId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String notificationType; // PUSH, EMAIL
    
    @Column(nullable = false)
    private String status; // SENT, FAILED
    
    private String errorMessage;
    
    @Column(nullable = false)
    private LocalDateTime sentAt;
    
    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
    
    public NotificationLog(Long eventId, Long userId, String notificationType, String status) {
        this.eventId = eventId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.status = status;
    }
}
