package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.response.ActivityFeedResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.service.IActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
@Tag(name = "Activity Feed", description = "Endpoints for fetching activity history in groups")
public class ActivityFeedController {

    private final IActivityLogService activityLogService;

    /**
     * GET /api/v1/activity/group/{groupId}
     *
     * Returns the activity feed for a specific group (newest first).
     * Used to display the "อัปเดต / กิจกรรม" tab in the app.
     *
     * @param groupId  The group ID
     * @param page     Page number (1-based, default 1)
     * @param size     Page size (default 20)
     */
    @GetMapping("/group/{groupId}")
    @Operation(
            summary = "Get group activity feed",
            description = "Returns all activity logs for a group (event created/updated/deleted, member added/removed), newest first."
    )
    public ResponseEntity<PaginationResponse<ActivityFeedResponse>> getGroupFeed(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginationResponse<ActivityFeedResponse> response =
                activityLogService.getGroupFeed(groupId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/activity/user/{userId}
     *
     * Returns the personal activity feed for a user
     * (covers all groups the user belongs to), newest first.
     * Shows actions of OTHER users in those groups.
     *
     * @param userId  The user ID
     * @param page    Page number (1-based, default 1)
     * @param size    Page size (default 20)
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get personal activity feed",
            description = "Returns activity logs across all groups the user belongs to (other users' actions only), newest first."
    )
    public ResponseEntity<PaginationResponse<ActivityFeedResponse>> getUserFeed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginationResponse<ActivityFeedResponse> response =
                activityLogService.getUserFeed(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/activity/user/{userId}/actions
     *
     * Returns only the actions that THIS user personally performed
     * (actorId = userId). Shows what the user created, updated, deleted, etc.
     *
     * How the system knows WHO did what:
     *  - Every time an action is performed (create/update/delete event, add/remove member),
     *    EventService or GroupService calls activityLogService.record(...) and passes
     *    the userId of the person who triggered the request as "actorId".
     *  - That actorId is stored permanently in the activity_logs table.
     *  - This endpoint filters by actorId = userId.
     *
     * @param userId  The user ID
     * @param page    Page number (1-based, default 1)
     * @param size    Page size (default 20)
     */
    @GetMapping("/user/{userId}/actions")
    @Operation(
            summary = "Get actions performed by a user",
            description = "Returns only the activity logs where this user was the actor (created/updated/deleted events, added/removed members)."
    )
    public ResponseEntity<PaginationResponse<ActivityFeedResponse>> getUserActions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        PaginationResponse<ActivityFeedResponse> response =
                activityLogService.getUserActions(userId, page, size);
        return ResponseEntity.ok(response);
    }
}

