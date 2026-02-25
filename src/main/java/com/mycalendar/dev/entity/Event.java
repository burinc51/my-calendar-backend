package com.mycalendar.dev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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

    private LocalDateTime notificationTime; // Notification time setting
    private String notificationType; // POPUP, EMAIL, PUSH
    private Integer remindBeforeMinutes; // Minutes to remind before

    private String repeatType; // NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
    private LocalDateTime repeatUntil; // Repeat end date

    private String color;
    private String category;
    private String priority;

    private Boolean pinned = false;
    private String imageUrl; // Image path storage
    private Long createById;
    private Boolean allDay = false;
    
    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean notificationSent = false; // Flag indicating if notification has been sent

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
