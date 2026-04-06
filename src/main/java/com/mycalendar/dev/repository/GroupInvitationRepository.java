package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.GroupInvitation;
import com.mycalendar.dev.enums.GroupInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    boolean existsByGroupGroupIdAndInvitedUserUserIdAndStatus(Long groupId, Long invitedUserId, GroupInvitationStatus status);

    List<GroupInvitation> findByInvitedUserUserIdAndStatusOrderByInvitedAtDesc(Long invitedUserId, GroupInvitationStatus status);

    List<GroupInvitation> findByGroupGroupIdAndStatusOrderByInvitedAtDesc(Long groupId, GroupInvitationStatus status);

    Optional<GroupInvitation> findByInvitationIdAndInvitedUserUserId(Long invitationId, Long invitedUserId);
}

