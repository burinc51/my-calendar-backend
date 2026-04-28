package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.ActivityLog;
import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.GroupInvitation;
import com.mycalendar.dev.entity.PushToken;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.payload.response.ActivityFeedResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.repository.ActivityLogRepository;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.GroupInvitationRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService implements IActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final PushTokenRepository pushTokenRepository;
    private final ExpoPushService expoPushService;

    /**
     * Record an activity entry and optionally broadcast a push notification
     * to all group members (except the actor who triggered the action).
     * Pass skipActivityPush=true when the creator is the only assignee —
     * no one else needs to be notified about the event creation.
     */
    @Override
    @Transactional
    public void record(Long groupId, Long actorId,
                       String actionType,
                       Long eventId, String eventTitle,
                       Long targetUserId, String targetUserName,
                       boolean skipActivityPush) {
        recordInternal(groupId, actorId, actionType, eventId, eventTitle, targetUserId, targetUserName, null, null, skipActivityPush);
    }

    @Override
    @Transactional
    public void record(Long groupId, Long actorId,
                       String actionType,
                       Long eventId, String eventTitle,
                       Long targetUserId, String targetUserName,
                       Long invitationId,
                       boolean skipActivityPush) {
        recordInternal(groupId, actorId, actionType, eventId, eventTitle, targetUserId, targetUserName, null, invitationId, skipActivityPush);
    }

    @Override
    @Transactional
    public void record(Long groupId, Long actorId,
                       String actionType,
                       Long eventId, String eventTitle,
                       Long targetUserId, String targetUserName,
                       String actionDetail,
                       boolean skipActivityPush) {
        recordInternal(groupId, actorId, actionType, eventId, eventTitle, targetUserId, targetUserName, actionDetail, null, skipActivityPush);
    }

    @Override
    @Transactional
    public void record(Long groupId, Long actorId,
                       String actionType,
                       Long eventId, String eventTitle,
                       Long targetUserId, String targetUserName,
                       String actionDetail,
                       Long invitationId,
                       boolean skipActivityPush) {
        recordInternal(groupId, actorId, actionType, eventId, eventTitle, targetUserId, targetUserName, actionDetail, invitationId, skipActivityPush);
    }

    @Override
    @Transactional
    public void updateInvitationStatus(Long groupId,
                                       Long responderUserId,
                                       String actionType,
                                       Long targetUserId,
                                       String targetUserName,
                                       Long invitationId,
                                       boolean skipActivityPush) {
        if (invitationId == null) {
            recordInternal(groupId, responderUserId, actionType, null, null, targetUserId, targetUserName, null, null, skipActivityPush);
            return;
        }

        ActivityLog invitationLog = activityLogRepository
                .findTopByInvitationIdAndActionTypeOrderByIdDesc(invitationId, "INVITATION_SENT")
                .orElse(null);

        if (invitationLog == null) {
            Long actorId = responderUserId;
            String actorName = "Unknown";
            String actorAvatar = null;

            GroupInvitation invitation = groupInvitationRepository.findById(invitationId).orElse(null);
            if (invitation != null && invitation.getInviterUser() != null) {
                actorId = invitation.getInviterUser().getUserId();
                actorName = invitation.getInviterUser().getName();
                actorAvatar = invitation.getInviterUser().getPictureUrl();
            }

             ActivityLog newInvitationLog = new ActivityLog();
             newInvitationLog.setGroupId(groupId);
             newInvitationLog.setActorId(actorId);
             newInvitationLog.setActorName(actorName);
             newInvitationLog.setActorAvatar(actorAvatar);
             newInvitationLog.setActionType(actionType);
             newInvitationLog.setTargetUserId(targetUserId);
             newInvitationLog.setTargetUserName(targetUserName);

             // Resolve target user avatar snapshot
             String targetAvatar = null;
             if (targetUserId != null) {
                 try {
                     User targetUser = userRepository.findById(targetUserId).orElse(null);
                     if (targetUser != null) {
                         targetAvatar = targetUser.getPictureUrl();
                     }
                 } catch (Exception e) {
                     log.warn("Could not resolve target user avatar for userId={}", targetUserId);
                 }
             }
             newInvitationLog.setTargetAvatar(targetAvatar);
             newInvitationLog.setInvitationId(invitationId);
            newInvitationLog.setCreatedAt(LocalDateTime.now());
            activityLogRepository.save(newInvitationLog);

            if (!skipActivityPush) {
                sendActivityPushNotification(groupId, responderUserId, actorName,
                        actionType, null, targetUserId, targetUserName);
            }
            return;
        }

        invitationLog.setActionType(actionType);
        invitationLog.setTargetUserId(targetUserId);
        invitationLog.setTargetUserName(targetUserName);
        invitationLog.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(invitationLog);

        if (skipActivityPush) {
            return;
        }

        sendActivityPushNotification(groupId, responderUserId, invitationLog.getActorName(),
                actionType, null, targetUserId, targetUserName);
    }

    private void recordInternal(Long groupId, Long actorId,
                                String actionType,
                                Long eventId, String eventTitle,
                                Long targetUserId, String targetUserName,
                                String actionDetail,
                                Long invitationId,
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

         // Resolve target user avatar snapshot
         String targetAvatar = null;
         if (targetUserId != null) {
             try {
                 User targetUser = userRepository.findById(targetUserId).orElse(null);
                 if (targetUser != null) {
                     targetAvatar = targetUser.getPictureUrl();
                 }
             } catch (Exception e) {
                 log.warn("Could not resolve target user avatar for userId={}", targetUserId);
             }
         }
         activityLog.setTargetAvatar(targetAvatar);

         activityLog.setActionDetail(actionDetail);
         activityLog.setInvitationId(invitationId);
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
     * Sends a push notification to the right audience for each activity type.
     *
     * Notification format examples:
     *  - EVENT_CREATED : "แคว สร้างกิจกรรม 'ประชุม'"
     *  - MEMBER_ADDED  : "ต้น ใหม่ เข้ากลุ่ม Team"
     *  - INVITATION_SENT : "แคว เชิญ ต้น เข้าร่วมกลุ่ม Team"
     */
    private void sendActivityPushNotification(Long groupId, Long actorId, String actorName,
                                               String actionType, String eventTitle,
                                               Long targetUserId, String targetUserName) {
        try {
            List<Long> recipientIds = new ArrayList<>();

            if ("INVITATION_SENT".equals(actionType)) {
                if (targetUserId != null && !targetUserId.equals(actorId)) {
                    recipientIds.add(targetUserId);
                }
            } else {
                recipientIds.addAll(userGroupRepository.findUserIdsByGroupId(groupId)
                        .stream()
                        .filter(id -> !id.equals(actorId))
                            .filter(id -> !("MEMBER_ADDED".equals(actionType)
                                    && id.equals(targetUserId)))
                        .collect(Collectors.toList()));

                if ("MEMBER_REMOVED".equals(actionType)
                        && targetUserId != null
                        && !targetUserId.equals(actorId)
                        && !recipientIds.contains(targetUserId)) {
                    recipientIds.add(targetUserId);
                }
            }

            if (recipientIds.isEmpty()) {
                log.debug("No members to notify for group {} [{}]", groupId, actionType);
                return;
            }

            // Get active push tokens for all those members
            List<PushToken> tokens = pushTokenRepository.findActiveTokensByUserIds(recipientIds);
            if (tokens.isEmpty()) {
                log.debug("No active push tokens for recipients of group {} [{}]", groupId, actionType);
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
            case "INVITATION_SENT" -> "✉️ คำเชิญเข้ากลุ่ม";
            case "INVITATION_ACCEPTED" -> "✅ รับคำเชิญแล้ว";
            case "INVITATION_REJECTED" -> "❌ ปฏิเสธคำเชิญ";
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
            case "INVITATION_SENT" -> actorName + " เชิญ " + targetUserName + " เข้าร่วมกลุ่ม " + groupName;
            case "INVITATION_ACCEPTED" -> targetUserName + " ตอบรับคำเชิญเข้ากลุ่ม " + groupName;
            case "INVITATION_REJECTED" -> targetUserName + " ปฏิเสธคำเชิญเข้ากลุ่ม " + groupName;
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
        List<ActivityLog> logs = logPage.getContent();

        Map<Long, Group> groupById = groupRepository.findAllById(
                        logs.stream()
                                .map(ActivityLog::getGroupId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Group::getGroupId, Function.identity()));

        Map<Long, Event> eventById = eventRepository.findAllById(
                        logs.stream()
                                .map(ActivityLog::getEventId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Event::getEventId, Function.identity()));

        List<ActivityFeedResponse> content = logs.stream()
                .map(v -> toResponse(v, groupById, eventById))
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

    private ActivityFeedResponse toResponse(ActivityLog a,
                                            Map<Long, Group> groupById,
                                            Map<Long, Event> eventById) {
        Group group = a.getGroupId() == null ? null : groupById.get(a.getGroupId());
        Event event = a.getEventId() == null ? null : eventById.get(a.getEventId());

        return ActivityFeedResponse.builder()
                .id(a.getId())
                .groupId(a.getGroupId())
                .groupName(group == null ? null : group.getGroupName())
                .groupColor(group == null ? null : group.getColor())
                .actorId(a.getActorId())
                .actorName(a.getActorName())
                .actorAvatar(a.getActorAvatar())
                .actionType(a.getActionType())
                .eventId(a.getEventId())
                .eventTitle(a.getEventTitle())
                .eventColor(event == null ? null : event.getColor())
                .eventStartDate(event == null ? null : event.getStartDate())
                .eventEndDate(event == null ? null : event.getEndDate())
                .targetUserId(a.getTargetUserId())
                .targetUserName(a.getTargetUserName())
                .targetAvatar(a.getTargetAvatar())
                .invitationId(a.getInvitationId())
                .actionDetail(a.getActionDetail())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
