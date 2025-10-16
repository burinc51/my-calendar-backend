package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.UserGroup;
import com.mycalendar.dev.entity.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId> {
    boolean existsByUserUserIdAndGroupGroupId(Long userId, Long groupId);

    Optional<UserGroup> findByUserUserIdAndGroupGroupId(Long userId, Long groupId);
}
