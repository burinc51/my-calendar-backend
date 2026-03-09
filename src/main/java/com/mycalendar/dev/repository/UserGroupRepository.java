package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.UserGroup;
import com.mycalendar.dev.entity.UserGroupId;
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
}
