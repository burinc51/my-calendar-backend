package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Fetch all activity logs for a given group, newest first.
     */
    Page<ActivityLog> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    /**
     * Personal feed behavior:
     *  1) Current groups: show all actions (including this user's own actions),
     *     except GROUP_CREATED by this user.
     *  2) Left groups: still keep historical logs where this user was directly involved
     *     (as actor or target), while excluding GROUP_CREATED by this user.
     */
    @Query("""
            SELECT a FROM ActivityLog a
            WHERE (
                (
                    a.groupId IN (
                        SELECT ug.id.groupId FROM UserGroup ug WHERE ug.id.userId = :userId
                    )
                    AND NOT (a.actionType = 'MEMBER_ADDED' AND a.targetUserId = :userId)
                    AND NOT (a.actionType = 'MEMBER_REMOVED' AND a.targetUserId = :userId)
                )
                OR (
                    a.groupId NOT IN (
                        SELECT ug.id.groupId FROM UserGroup ug WHERE ug.id.userId = :userId
                    )
                    AND (a.actorId = :userId OR a.targetUserId = :userId)
                )
            )
            AND NOT (a.actionType = 'GROUP_CREATED' AND a.actorId = :userId)
            ORDER BY a.createdAt DESC
            """)
    Page<ActivityLog> findFeedByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Fetch only actions PERFORMED BY a specific user (actorId = userId), newest first.
     * Used to show "what this user has done".
     */
    Page<ActivityLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);

    /**
     * Count logs per group (for badge numbers etc.).
     */
    long countByGroupId(Long groupId);
}
