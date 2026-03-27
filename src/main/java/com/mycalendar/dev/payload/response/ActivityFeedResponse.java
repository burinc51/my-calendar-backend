package com.mycalendar.dev.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO returned by the Activity Feed API.
 * Each item represents one action that occurred in a group.
 */
@Builder
public record ActivityFeedResponse(
        Long id,

        /** Group the activity belongs to */
        Long groupId,

        /** User who performed the action */
        Long actorId,
        String actorName,
        String actorAvatar,

        /**
         * Type of action.
         * EVENT_CREATED | EVENT_UPDATED | EVENT_DELETED |
         * MEMBER_ADDED  | MEMBER_REMOVED |
         * GROUP_CREATED | GROUP_UPDATED  | GROUP_DELETED
         */
        String actionType,

        /** Populated when actionType is event-related */
        Long eventId,
        String eventTitle,

        /** Populated when actionType is member-related */
        Long targetUserId,
        String targetUserName,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}

