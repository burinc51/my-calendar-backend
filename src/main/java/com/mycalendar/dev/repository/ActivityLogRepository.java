package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Fetch all activity logs for a given group, newest first.
     */
    Page<ActivityLog> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    /**
     * Fetch logs for all groups that a user belongs to (personal feed - sees everyone's actions).
     */
    @Query("""
            SELECT a FROM ActivityLog a
            WHERE a.groupId IN (
                SELECT ug.group.groupId FROM UserGroup ug WHERE ug.user.userId = :userId
            )
            AND a.actorId <> :userId
            AND NOT (a.actionType = 'MEMBER_ADDED' AND a.targetUserId = :userId)
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

