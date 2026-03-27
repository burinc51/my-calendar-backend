package com.mycalendar.dev.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "notes", indexes = {
        @Index(name = "idx_notes_user_id", columnList = "user_id"),
        @Index(name = "idx_notes_is_pinned", columnList = "is_pinned"),
        @Index(name = "idx_notes_updated_at", columnList = "updated_at")
})
@Getter
@Setter
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title", length = 255)
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag", length = 100, nullable = false)
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name = "reminder_date")
    private OffsetDateTime reminderDate;

    @Column(name = "recurrence", length = 20, nullable = false)
    private String recurrence = "none";

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(name = "location_link", length = 2000)
    private String locationLink;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

