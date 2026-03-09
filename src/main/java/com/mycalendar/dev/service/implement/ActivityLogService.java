package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.ActivityLog;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.payload.response.ActivityFeedResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.repository.ActivityLogRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService implements IActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * Record an activity entry.
     * This is called internally by EventService and GroupService after every
     * significant action (create / update / delete event, add / remove member, etc.)
     */
    @Override
    @Transactional
    public void record(Long groupId, Long actorId,
                       String actionType,
                       Long eventId, String eventTitle,
                       Long targetUserId, String targetUserName) {

        // Resolve actor name (use stored name as snapshot, fall back to "Unknown")
        String actorName = "Unknown";
        try {
            User actor = userRepository.findById(actorId).orElse(null);
            if (actor != null) {
                actorName = actor.getName();
            }
        } catch (Exception e) {
            log.warn("Could not resolve actor name for userId={}", actorId);
        }

        ActivityLog log = new ActivityLog();
        log.setGroupId(groupId);
        log.setActorId(actorId);
        log.setActorName(actorName);
        log.setActorAvatar(null); // Reserved for future profile-picture support
        log.setActionType(actionType);
        log.setEventId(eventId);
        log.setEventTitle(eventTitle);
        log.setTargetUserId(targetUserId);
        log.setTargetUserName(targetUserName);

        activityLogRepository.save(log);
    }

    /**
     * Returns a paginated activity feed for a specific group,
     * ordered newest-first.
     */
    @Override
    public PaginationResponse<ActivityFeedResponse> getGroupFeed(Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ActivityLog> logPage = activityLogRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId, pageable);

        List<ActivityFeedResponse> content = logPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PaginationResponse.<ActivityFeedResponse>builder()
                .content(content)
                .pageNo(page)
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }

    /**
     * Returns a paginated personal feed for a user,
     * covering all groups the user belongs to, newest-first.
     */
    @Override
    public PaginationResponse<ActivityFeedResponse> getUserFeed(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ActivityLog> logPage = activityLogRepository.findFeedByUserId(userId, pageable);

        List<ActivityFeedResponse> content = logPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PaginationResponse.<ActivityFeedResponse>builder()
                .content(content)
                .pageNo(page)
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }

    // ── private mapper ────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of actions PERFORMED BY a specific user.
     * Only shows what that user personally did (actorId = userId).
     * Example: user สร้าง event ไหน, ลบ event ไหน, เพิ่มสมาชิกไหน
     */
    @Override
    public PaginationResponse<ActivityFeedResponse> getUserActions(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ActivityLog> logPage = activityLogRepository
                .findByActorIdOrderByCreatedAtDesc(userId, pageable);

        List<ActivityFeedResponse> content = logPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PaginationResponse.<ActivityFeedResponse>builder()
                .content(content)
                .pageNo(page)
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }

    private ActivityFeedResponse toResponse(ActivityLog a) {
        return ActivityFeedResponse.builder()
                .id(a.getId())
                .groupId(a.getGroupId())
                .actorId(a.getActorId())
                .actorName(a.getActorName())
                .actorAvatar(a.getActorAvatar())
                .actionType(a.getActionType())
                .eventId(a.getEventId())
                .eventTitle(a.getEventTitle())
                .targetUserId(a.getTargetUserId())
                .targetUserName(a.getTargetUserName())
                .createdAt(a.getCreatedAt())
                .build();
    }
}

