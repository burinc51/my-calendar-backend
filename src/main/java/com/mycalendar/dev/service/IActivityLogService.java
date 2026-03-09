package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.response.ActivityFeedResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;

public interface IActivityLogService {

    /**
     * Record a new activity in the log.
     *
     * @param groupId       Group that the activity belongs to
     * @param actorId       User who performed the action
     * @param actionType    One of: EVENT_CREATED, EVENT_UPDATED, EVENT_DELETED,
     *                      MEMBER_ADDED, MEMBER_REMOVED, GROUP_CREATED, GROUP_UPDATED, GROUP_DELETED
     * @param eventId       Event involved (nullable)
     * @param eventTitle    Snapshot of the event title (nullable)
     * @param targetUserId  Target user for MEMBER_* actions (nullable)
     * @param targetUserName Snapshot of the target user's name (nullable)
     */
    void record(Long groupId, Long actorId,
                String actionType,
                Long eventId, String eventTitle,
                Long targetUserId, String targetUserName);

    /**
     * Get a paginated activity feed for a specific group.
     */
    PaginationResponse<ActivityFeedResponse> getGroupFeed(Long groupId, int page, int size);

    /**
     * Get a paginated personal activity feed for a user
     * (all groups the user belongs to — sees everyone's actions in those groups).
     */
    PaginationResponse<ActivityFeedResponse> getUserFeed(Long userId, int page, int size);

    /**
     * Get a paginated list of actions PERFORMED BY a specific user
     * (only things that user personally did: created event, deleted event, added member, etc.).
     */
    PaginationResponse<ActivityFeedResponse> getUserActions(Long userId, int page, int size);
}

