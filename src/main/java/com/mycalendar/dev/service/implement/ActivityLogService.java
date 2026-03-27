package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.ActivityLog;
import com.mycalendar.dev.entity.PushToken;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.payload.response.ActivityFeedResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.repository.ActivityLogRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.PushTokenRepository;
import com.mycalendar.dev.repository.UserGroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.ExpoPushService;
import com.mycalendar.dev.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService implements IActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final PushTokenRepository pushTokenRepository;
    private final ExpoPushService expoPushService;

    /**
     * Record an activity entry and optionally broadcast a push notification
     * to all group members (except the actor who triggered the action).
     *
     * Pass skipActivityPush=true when the creator is the only assignee —
     * no one else needs to be notified about the event creation.
     */
    @Override
    public void record(Long groupId, Long actorId,
                       String actionType,
                       Long eventId, String eventTitle,
                       Long targetUserId, String targetUserName,
                       boolean skipActivityPush) {
        record(groupId, actorId, actionType, eventId, eventTitle, targetUserId, targetUserName, null, skipActivityPush);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long groupId, Long actorId,
                       String actionType,
                       Long eventId, String eventTitle,
                       Long targetUserId, String targetUserName,
                       String actionDetail,
                       boolean skipActivityPush) {

        // Resolve actor snapshot (name + avatar) so feed stays stable over time.
        String actorName = "Unknown";
        String actorAvatar = null;
        try {
            User actor = userRepository.findById(actorId).orElse(null);
            if (actor != null) {
                actorName = actor.getName();
                actorAvatar = actor.getPictureUrl();
            }
        } catch (Exception e) {
            log.warn("Could not resolve actor snapshot for userId={}", actorId);
        }

        // Save activity log (always recorded regardless of skipActivityPush)
        ActivityLog activityLog = new ActivityLog();
        activityLog.setGroupId(groupId);
        activityLog.setActorId(actorId);
        activityLog.setActorName(actorName);
        activityLog.setActorAvatar(actorAvatar);
        activityLog.setActionType(actionType);
        activityLog.setEventId(eventId);
        activityLog.setEventTitle(eventTitle);
        activityLog.setTargetUserId(targetUserId);
        activityLog.setTargetUserName(targetUserName);
        activityLog.setActionDetail(actionDetail);
        activityLogRepository.save(activityLog);

        // Skip push when creator is the only assignee (no one else to notify)
        if (skipActivityPush) {
            log.debug("⏭️ Skipping activity push for {} — creator is the only assignee", actionType);
            return;
        }

        // Send push notification to all group members (except the actor)
        sendActivityPushNotification(groupId, actorId, actorName,
                actionType, eventTitle, targetUserId, targetUserName);
    }

    /**
     * Sends a push notification to every group member who is NOT the actor.
     *
     * Notification format examples:
     *  - EVENT_CREATED : "แคว สร้างกิจกรรม 'ประชุม'"
     *  - EVENT_UPDATED : "แคว แก้ไขกิจกรรม 'ประชุม'"
     *  - EVENT_DELETED : "แคว ลบกิจกรรม 'ประชุม'"
     *  - MEMBER_ADDED  : "แคว เพิ่มสมาชิก ต้น เข้ากลุ่ม"
     *  - MEMBER_REMOVED: "แคว ลบสมาชิก ต้น ออกจากกลุ่ม"
     *  - GROUP_CREATED : "แคว สร้างกลุ่มใหม่"
     *  - GROUP_DELETED : "แคว ลบกลุ่ม"
     */
    private void sendActivityPushNotification(Long groupId, Long actorId, String actorName,
                                               String actionType, String eventTitle,
                                               Long targetUserId, String targetUserName) {
        try {
            // Build recipient list from current members, excluding actor.
            List<Long> memberIds = new ArrayList<>(userGroupRepository.findUserIdsByGroupId(groupId)
                    .stream()
                    .filter(id -> !id.equals(actorId))
                    // For MEMBER_ADDED, never notify the newly added member about their own join.
                    .filter(id -> !("MEMBER_ADDED".equals(actionType)
                            && targetUserId != null
                            && id.equals(targetUserId)))
                    .toList());

            // For MEMBER_REMOVED, include the removed user so they know why the group disappeared.
            if ("MEMBER_REMOVED".equals(actionType)
                    && targetUserId != null
                    && !targetUserId.equals(actorId)
                    && !memberIds.contains(targetUserId)) {
                memberIds.add(targetUserId);
            }

            if (memberIds.isEmpty()) {
                log.debug("No members to notify for group {} [{}]", groupId, actionType);
                return;
            }

            // Get active push tokens for all those members
            List<PushToken> tokens = pushTokenRepository.findActiveTokensByUserIds(memberIds);
            if (tokens.isEmpty()) {
                log.debug("No active push tokens for group {} members", groupId);
                return;
            }

            // Build notification title and body from actionType
            String title = buildNotificationTitle(actionType);
            String groupName = groupRepository.findById(groupId)
                    .map(com.mycalendar.dev.entity.Group::getGroupName)
                    .orElse("");
            String body  = buildNotificationBody(actorName, actionType, eventTitle, targetUserName, groupName);

            // Extra data sent alongside the notification (for deep-linking in the app)
            Map<String, Object> data = new HashMap<>();
            data.put("type", "activity");
            data.put("actionType", actionType);
            data.put("groupId", groupId);

            log.info("📢 Sending activity push to {} device(s) in group {} [{}]",
                    tokens.size(), groupId, actionType);

            // Send to each device
            for (PushToken token : tokens) {
                expoPushService.sendPushNotification(token.getToken(), title, body, data);
            }

        } catch (Exception e) {
            // Never let push notification failure break the main transaction
            log.error("Failed to send activity push notification for group {}: {}", groupId, e.getMessage());
        }
    }

    /**
     * Returns a short notification title based on the action type.
     */
    private String buildNotificationTitle(String actionType) {
        return switch (actionType) {
            case "EVENT_CREATED"  -> "📅 กิจกรรมใหม่";
            case "EVENT_UPDATED"  -> "✏️ กิจกรรมถูกแก้ไข";
            case "EVENT_DELETED"  -> "🗑️ กิจกรรมถูกลบ";
            case "MEMBER_ADDED"   -> "👤 เพิ่มสมาชิกใหม่";
            case "MEMBER_REMOVED" -> "👤 สมาชิกถูกลบ";
            case "GROUP_CREATED"  -> "🎉 สร้างกลุ่มใหม่";
            case "GROUP_UPDATED"  -> "✏️ กลุ่มถูกแก้ไข";
            case "GROUP_DELETED"  -> "🗑️ กลุ่มถูกลบ";
            default               -> "🔔 อัปเดตกลุ่ม";
        };
    }

    /**
     * Returns a human-readable notification body.
     */
    private String buildNotificationBody(String actorName, String actionType,
                                          String eventTitle, String targetUserName,
                                          String groupName) {
        return switch (actionType) {
            case "EVENT_CREATED"  -> actorName + " สร้างกิจกรรม \"" + eventTitle + "\"";
            case "EVENT_UPDATED"  -> actorName + " แก้ไขกิจกรรม \"" + eventTitle + "\"";
            case "EVENT_DELETED"  -> actorName + " ลบกิจกรรม \"" + eventTitle + "\"";
            case "MEMBER_ADDED"   -> targetUserName + " ใหม่ เข้ากลุ่ม " + groupName;
            case "MEMBER_REMOVED" -> actorName + " ลบสมาชิก " + targetUserName + " ออกจากกลุ่ม";
            case "GROUP_CREATED"  -> actorName + " สร้างกลุ่มใหม่";
            case "GROUP_UPDATED"  -> actorName + " แก้ไขข้อมูลกลุ่ม";
            case "GROUP_DELETED"  -> actorName + " ลบกลุ่ม";
            default               -> actorName + " มีการเปลี่ยนแปลงในกลุ่ม";
        };
    }

    // ── Feed queries ──────────────────────────────────────────────────────────

    @Override
    public PaginationResponse<ActivityFeedResponse> getGroupFeed(Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ActivityLog> logPage = activityLogRepository
                .findByGroupIdOrderByCreatedAtDesc(groupId, pageable);
        return toPageResponse(logPage, page);
    }

    @Override
    public PaginationResponse<ActivityFeedResponse> getUserFeed(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ActivityLog> logPage = activityLogRepository.findFeedByUserId(userId, pageable);
        return toPageResponse(logPage, page);
    }

    @Override
    public PaginationResponse<ActivityFeedResponse> getUserActions(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ActivityLog> logPage = activityLogRepository
                .findByActorIdOrderByCreatedAtDesc(userId, pageable);
        return toPageResponse(logPage, page);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PaginationResponse<ActivityFeedResponse> toPageResponse(Page<ActivityLog> logPage, int page) {
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
                .actionDetail(a.getActionDetail())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
