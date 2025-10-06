package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", length = 10000)
    private String description;

    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    @Column(nullable = false)
    private boolean isPinned = false;
    private LocalDateTime notificationTime;
    private String repeating;
    private String color;
    private String category;
    private String priority;
    private Long groupId;

    @ManyToMany
    @JoinTable(
            name = "event_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;


}