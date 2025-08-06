package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupGroupIdAndUserId(Long groupId, Long assigneeId);
}