package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Stores a record of every significant action performed in a group
 * (event created/updated/deleted, member added/removed, etc.)
 * so the client can display an activity feed.
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_log_group", columnList = "group_id"),
        @Index(name = "idx_activity_log_actor", columnList = "actor_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The group this activity belongs to.
     */
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    /**
     * User who performed the action.
     */
    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    /**
     * Display name of the actor (snapshot so it stays correct even if user renames).
     */
    @Column(name = "actor_name")
    private String actorName;

    /**
     * Avatar / profile-picture URL of the actor at the time of action.
     */
    @Column(name = "actor_avatar")
    private String actorAvatar;

    /**
     * Type of action performed.
     * Values: EVENT_CREATED, EVENT_UPDATED, EVENT_DELETED,
     *         MEMBER_ADDED, MEMBER_REMOVED, GROUP_CREATED, GROUP_UPDATED, GROUP_DELETED
     */
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    /**
     * Optional reference to the event involved, if any.
     */
    @Column(name = "event_id")
    private Long eventId;

    /**
     * Human-readable title of the event at the time of action (snapshot).
     */
    @Column(name = "event_title")
    private String eventTitle;

    /**
     * Optional: the target user (e.g. member who was added or removed).
     */
    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_user_name")
    private String targetUserName;

    /**
     * Optional: invitation reference for invitation-related actions.
     */
    @Column(name = "invitation_id")
    private Long invitationId;

    /**
     * Timestamp when the action occurred.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Optional details about the action, for example, what exactly was updated in an event.
     */
    @Column(name = "action_detail", length = 1000)
    private String actionDetail;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── convenience constructors ──────────────────────────────────────────────

    /** Constructor for event-related actions. */
    public ActivityLog(Long groupId, Long actorId, String actorName, String actorAvatar,
                       String actionType, Long eventId, String eventTitle) {
        this.groupId    = groupId;
        this.actorId    = actorId;
        this.actorName  = actorName;
        this.actorAvatar = actorAvatar;
        this.actionType = actionType;
        this.eventId    = eventId;
        this.eventTitle = eventTitle;
    }

    /** Constructor for member-related actions. */
    public ActivityLog(Long groupId, Long actorId, String actorName, String actorAvatar,
                       String actionType, Long targetUserId, String targetUserName, boolean isMemberAction) {
        this.groupId        = groupId;
        this.actorId        = actorId;
        this.actorName      = actorName;
        this.actorAvatar    = actorAvatar;
        this.actionType     = actionType;
        this.targetUserId   = targetUserId;
        this.targetUserName = targetUserName;
    }
}
