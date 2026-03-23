package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.UserGroup;
import com.mycalendar.dev.entity.UserGroupId;
import com.mycalendar.dev.projection.GroupMemberProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId> {
    boolean existsByUserUserIdAndGroupGroupId(Long userId, Long groupId);

    Optional<UserGroup> findByUserUserIdAndGroupGroupId(Long userId, Long groupId);

    /**
     * Returns all user IDs that belong to a given group.
     * Used for broadcasting push notifications to all group members.
     */
    @Query("SELECT ug.user.userId FROM UserGroup ug WHERE ug.group.groupId = :groupId")
    List<Long> findUserIdsByGroupId(@Param("groupId") Long groupId);

    @Query("""
            SELECT
                u.userId AS userId,
                u.name AS name,
                u.username AS username,
                p.permissionName AS permissionName,
                usp.pictureUrl AS pictureUrl
            FROM UserGroup ug
                JOIN ug.user u
                JOIN ug.permission p
                LEFT JOIN UserSocialProvider usp
                    ON usp.user.userId = u.userId
                    AND usp.id = (
                        SELECT MAX(usp2.id)
                        FROM UserSocialProvider usp2
                        WHERE usp2.user.userId = u.userId
                    )
            WHERE ug.group.groupId = :groupId
            ORDER BY u.userId
            """)
    List<GroupMemberProjection> findMembersByGroupId(@Param("groupId") Long groupId);
}
