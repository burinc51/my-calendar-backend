package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Lob
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}