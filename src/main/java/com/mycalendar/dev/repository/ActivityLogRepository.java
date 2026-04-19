package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Fetch all activity logs for a given group, newest first.
     */
    Page<ActivityLog> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    /**
     * Personal feed behavior:
     *  1) Show group activity only from the moment the user joined that group.
     *  2) Keep invitation rows visible only to the invited/responder user.
     */
    @Query(
            "SELECT a FROM ActivityLog a " +
            "WHERE ( " +
            "    a.actionType NOT IN ('INVITATION_SENT') " +
            "    AND EXISTS ( " +
            "        SELECT ug FROM UserGroup ug " +
            "        WHERE ug.id.userId = :userId " +
            "          AND ug.id.groupId = a.groupId " +
            "    ) " +
            "    AND ( " +
            "        ( " +
            "            SELECT MAX(joinLog.createdAt) " +
            "            FROM ActivityLog joinLog " +
            "            WHERE joinLog.groupId = a.groupId " +
            "              AND ( " +
            "                    (joinLog.actionType = 'GROUP_CREATED' AND joinLog.actorId = :userId) " +
            "                 OR (joinLog.actionType = 'MEMBER_ADDED' AND joinLog.targetUserId = :userId) " +
            "                 OR (joinLog.actionType = 'INVITATION_ACCEPTED' AND joinLog.targetUserId = :userId) " +
            "              ) " +
            "        ) IS NULL " +
            "        OR a.createdAt >= ( " +
            "            SELECT MAX(joinLog.createdAt) " +
            "            FROM ActivityLog joinLog " +
            "            WHERE joinLog.groupId = a.groupId " +
            "              AND ( " +
            "                    (joinLog.actionType = 'GROUP_CREATED' AND joinLog.actorId = :userId) " +
            "                 OR (joinLog.actionType = 'MEMBER_ADDED' AND joinLog.targetUserId = :userId) " +
            "                 OR (joinLog.actionType = 'INVITATION_ACCEPTED' AND joinLog.targetUserId = :userId) " +
            "              ) " +
            "        ) " +
            "    ) " +
            "    AND NOT (a.actionType = 'MEMBER_ADDED' AND a.targetUserId = :userId) " +
            "    AND NOT (a.actionType = 'MEMBER_REMOVED' AND a.targetUserId = :userId) " +
            "    AND NOT (a.actionType = 'GROUP_CREATED' AND a.actorId = :userId) " +
            ") " +
            "OR ( " +
            "    a.actionType IN ('INVITATION_SENT', 'INVITATION_ACCEPTED', 'INVITATION_REJECTED') " +
            "    AND a.targetUserId = :userId " +
            ") " +
            "ORDER BY a.createdAt DESC"
    )
    Page<ActivityLog> findFeedByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find latest invitation-sent activity for a specific invitation.
     * Used to update one existing row when invitation is accepted/rejected.
     */
    Optional<ActivityLog> findTopByInvitationIdAndActionTypeOrderByIdDesc(Long invitationId, String actionType);

    /**
     * Fetch only actions PERFORMED BY a specific user (actorId = userId), newest first.
     * Used to show "what this user has done".
     */
    Page<ActivityLog> findByActorIdOrderByCreatedAtDesc(Long actorId, Pageable pageable);

}
