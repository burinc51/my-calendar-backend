package com.mycalendar.dev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_notification_time", columnList = "notification_time, notification_sent")
})
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description; // Note

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String location;
    private Double latitude;
    private Double longitude;

    private LocalDateTime notificationTime; // ตั้งเวลาแจ้งเตือน
    private String notificationType; // POPUP, EMAIL, PUSH
    private Integer remindBeforeMinutes; // แจ้งเตือนก่อนกี่นาที

    private String repeatType; // NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
    private LocalDateTime repeatUntil; // สิ้นสุดการทำซ้ำ

    private String color;
    private String category;
    private String priority;

    private Boolean pinned = false;
    private String imageUrl; // เก็บ path รูป
    private Long createById;
    private Boolean allDay = false;
    
    @Column(nullable = false)
    private Boolean notificationSent = false; // Flag ว่าส่ง notification ไปแล้วหรือยัง

    // Event belongs to Group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private Group group;

    // Event has many Users
    @ManyToMany
    @JoinTable(
            name = "event_user",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> users = new HashSet<>();
}
