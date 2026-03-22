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
    private String notificationType = "PUSH"; // Only PUSH is supported
    private Integer remindBeforeValue;  // Numeric value of the remind offset (e.g. 15, 2, 1)
    private String remindBeforeUnit;    // Unit of the offset: MINUTES, HOURS, DAYS, WEEKS
    // Computed field kept for backward-compat: total minutes = remindBeforeValue converted to minutes
    private Integer remindBeforeMinutes; // Minutes to remind before (auto-calculated)

    private String repeatType; // NONE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
    private LocalDateTime repeatUntil; // Repeat end date
    private Integer repeatInterval; // Repeat every N units (for example, every 2 weeks), default = 1
    private String repeatDays; // Repeat days for CUSTOM/WEEKLY, for example "MON,WED,FRI"

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
