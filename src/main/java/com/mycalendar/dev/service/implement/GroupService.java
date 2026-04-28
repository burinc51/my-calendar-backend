package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.GroupInvitation;
import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.entity.UserGroup;
import com.mycalendar.dev.enums.GroupInvitationStatus;
import com.mycalendar.dev.exception.ForbiddenException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.mapper.GroupMapper;
import com.mycalendar.dev.payload.request.GroupAddMemberRequest;
import com.mycalendar.dev.payload.request.GroupInviteUsersRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupInvitationBatchResponse;
import com.mycalendar.dev.payload.response.GroupInvitationResponse;
import com.mycalendar.dev.payload.response.GroupInvitationSkipResponse;
import com.mycalendar.dev.payload.response.GroupInvitableUserResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.GroupUserResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.projection.GroupProjection;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.GroupInvitationRepository;
import com.mycalendar.dev.repository.PermissionRepository;
import com.mycalendar.dev.repository.UserGroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IActivityLogService;
import com.mycalendar.dev.service.IGroupService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import com.mycalendar.dev.exception.APIException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class GroupService implements IGroupService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final UserGroupRepository userGroupRepository;
    private final IActivityLogService activityLogService;

    public GroupService(UserRepository userRepository, PermissionRepository permissionRepository,
                        GroupRepository groupRepository, GroupInvitationRepository groupInvitationRepository,
                        UserGroupRepository userGroupRepository,
                        IActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.groupRepository = groupRepository;
        this.groupInvitationRepository = groupInvitationRepository;
        this.userGroupRepository = userGroupRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public GroupResponse create(GroupRequest request) {
        Long creatorUserId = resolveCurrentUserId();
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new NotFoundException("User", "id", creatorUserId.toString()));

        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setIcon(request.getIcon());
        group.setColor(request.getColor());
        group.setBg(request.getBg());

        groupRepository.save(group);

        Permission adminPermission = permissionRepository.findByPermissionName("ADMIN")
                .orElseThrow(() -> new NotFoundException("Permission", "name", "ADMIN"));

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(creator);
        userGroup.setGroup(group);
        userGroup.setPermission(adminPermission);
        userGroupRepository.save(userGroup);

        // Record activity: group created
        groupRepository.flush();
        activityLogService.record(
                group.getGroupId(),
                creator.getUserId(),
                "GROUP_CREATED",
                null, null,
                null, null,
                true  // Skip notification for group creation
        );

        return GroupMapper.mapToDto(group);
    }

    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        Long userId = userRepository.findIdByUsername(username);
        if (userId == null) {
            throw new NotFoundException("User", "username", username);
        }

        return userId;
    }

    @Override
    public GroupResponse update(GroupRequest request, Long id) {
        Long actorUserId = resolveCurrentUserId();

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Group", "groupId", id.toString()));

        String actionDetail = buildGroupUpdateDetail(group, request);

        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setIcon(request.getIcon());
        group.setColor(request.getColor());
        group.setBg(request.getBg());

        groupRepository.save(group);

        activityLogService.record(
                group.getGroupId(),
                actorUserId,
                "GROUP_UPDATED",
                null, null,
                null, null,
                actionDetail,
                false
        );

        return GroupMapper.mapToDto(group);
    }

    @Override
    public GroupResponse getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .map(GroupMapper::mapToDto)
                .orElseThrow(() -> new NotFoundException("Group", "id", groupId.toString()));
    }

    @Override
    public PaginationResponse<GroupResponse> getAllGroup(PaginationRequest request) {
        Page<Group> pages = groupRepository.findAll(request.getPageRequest());

        List<GroupResponse> groups = pages.getContent().stream().map(GroupMapper::mapToDto).collect(Collectors.toList());

        return PaginationResponse.<GroupResponse>builder()
                .content(groups)
                .pageNo(request.getPageNumber())
                .pageSize(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }

    @Override
    public void addMemberToGroup(GroupAddMemberRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group", "id", request.getGroupId().toString()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User", "id", request.getUserId().toString()));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new NotFoundException("Permission", "id", request.getPermissionId().toString()));

        boolean exists = userGroupRepository.existsByUserUserIdAndGroupGroupId(user.getUserId(), group.getGroupId());
        if (exists) {
            throw new IllegalArgumentException("User is already in this group");
        }

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(user);
        userGroup.setGroup(group);
        userGroup.setPermission(permission);

        userGroupRepository.save(userGroup);

        // Record activity: member added
        // actorId — the person who performed the add (use request.getActorId() if present, else the added user)
        Long actorId = (request.getActorId() != null) ? request.getActorId() : user.getUserId();
        activityLogService.record(
                group.getGroupId(),
                actorId,
                "MEMBER_ADDED",
                null, null,
                user.getUserId(), user.getName(),
                false
        );
    }

    @Transactional
    public List<GroupResponse> getGroupsByUserId(Long userId) {
        List<GroupProjection> projections = groupRepository.findAllGroupsByUserId(userId);
        return GroupMapper.mapToDto(projections);
    }

    @Override
    @Transactional
    public List<GroupUserResponse> getUsersByGroupId(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new NotFoundException("Group", "id", groupId.toString());
        }

        return userGroupRepository.findMembersByGroupId(groupId).stream()
                .map(v -> GroupUserResponse.builder()
                        .userId(v.getUserId())
                        .username(v.getUsername())
                        .name(v.getName())
                        .imageUrl(v.getPictureUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<GroupInvitableUserResponse> getInvitableUsers(Long groupId, PaginationRequest request) {
        Long requestUserId = resolveCurrentUserId();

        if (!groupRepository.existsById(groupId)) {
            throw new NotFoundException("Group", "id", groupId.toString());
        }
        ensureAdminMember(groupId, requestUserId);

        Set<Long> memberUserIds = new HashSet<>(userGroupRepository.findUserIdsByGroupId(groupId));
        Set<Long> pendingInvitationUserIds = new HashSet<>(
                groupInvitationRepository.findInvitedUserIdsByGroupIdAndStatus(groupId, GroupInvitationStatus.PENDING)
        );

        // Extract name filter from request
        String nameFilter = "";
        if (request.getFilter() != null && request.getFilter().containsKey("name")) {
            Object nameValue = request.getFilter().get("name");
            if (nameValue != null) {
                nameFilter = nameValue.toString().trim();
            }
        }

        // Fetch candidate users with or without name filter
        Page<User> candidatePage;
        if (nameFilter.isEmpty()) {
            candidatePage = userRepository.findUsersByRoleNameExcludingUser("USER", requestUserId, request.getPageRequest());
        } else {
            candidatePage = userRepository.findUsersByRoleNameExcludingUserWithNameFilter("USER", requestUserId, nameFilter, request.getPageRequest());
        }

         List<GroupInvitableUserResponse> content = candidatePage.getContent()
                .stream()
                .map(user -> GroupInvitableUserResponse.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .imageUrl(user.getPictureUrl())
                        .inviteStatus(resolveInviteStatus(user.getUserId(), memberUserIds, pendingInvitationUserIds))
                        .build())
                .collect(Collectors.toList());

        return PaginationResponse.<GroupInvitableUserResponse>builder()
                .content(content)
                .pageNo(request.getPageNumber())
                .pageSize(candidatePage.getSize())
                .totalElements(candidatePage.getTotalElements())
                .totalPages(candidatePage.getTotalPages())
                .last(candidatePage.isLast())
                .build();
    }

    private String resolveInviteStatus(Long userId, Set<Long> memberUserIds, Set<Long> pendingInvitationUserIds) {
        if (memberUserIds.contains(userId)) {
            return "ALREADY_IN_GROUP";
        }
        if (pendingInvitationUserIds.contains(userId)) {
            return "INVITED";
        }
        return "INVITABLE";
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        Long currentUserId = resolveCurrentUserId();
        
        UserGroup userGroup = userGroupRepository.findByUserUserIdAndGroupGroupId(userId, groupId)
                .orElseThrow(() -> new NotFoundException("UserGroup", "user/group", userId + "/" + groupId));

        User removedUser = userGroup.getUser();
        userGroupRepository.delete(userGroup);

        // Record activity: member removed (actor is the admin removing, target is the removed user)
        activityLogService.record(
                groupId,
                currentUserId,
                "MEMBER_REMOVED",
                null, null,
                removedUser.getUserId(), removedUser.getName(),
                false
        );
    }

    @Transactional
    public void deleteGroup(Long groupId, Long requestUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group", "id", groupId.toString()));

        userRepository.findById(requestUserId)
                .orElseThrow(() -> new NotFoundException("User", "id", requestUserId.toString()));

        boolean isAdmin = group.getUserGroups().stream()
                .anyMatch(ug ->
                        ug.getUser().getUserId().equals(requestUserId)
                                && ug.getPermission().getPermissionName().equalsIgnoreCase("ADMIN")
                );

        if (!isAdmin) {
            throw new ForbiddenException("You do not have permission to delete this group");
        }

        // Record activity before deleting (while group entity still exists)
        activityLogService.record(
                groupId,
                requestUserId,
                "GROUP_DELETED",
                null, null,
                null, null,
                false
        );

        groupRepository.delete(group);
    }
    @Override
    @Transactional
    public GroupInvitationBatchResponse inviteUsers(Long groupId, GroupInviteUsersRequest request) {
        if (request == null || request.getUserIds() == null || request.getUserIds().isEmpty()) {
            throw new APIException(HttpStatus.BAD_REQUEST, "userIds is required");
        }

        Long inviterUserId = resolveCurrentUserId();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group", "id", groupId.toString()));
        User inviter = userRepository.findById(inviterUserId)
                .orElseThrow(() -> new NotFoundException("User", "id", inviterUserId.toString()));

        ensureAdminMember(groupId, inviterUserId);

        LinkedHashSet<Long> uniqueUserIds = new LinkedHashSet<>(request.getUserIds());
        List<GroupInvitationResponse> invitations = new ArrayList<>();
        List<GroupInvitationSkipResponse> skipped = new ArrayList<>();

        for (Long invitedUserId : uniqueUserIds) {
            if (invitedUserId == null) {
                skipped.add(GroupInvitationSkipResponse.builder()
                        .userId(null)
                        .reason("userId is required")
                        .build());
                continue;
            }

            if (invitedUserId.equals(inviterUserId)) {
                skipped.add(GroupInvitationSkipResponse.builder()
                        .userId(invitedUserId)
                        .reason("Cannot invite yourself")
                        .build());
                continue;
            }

            User invitedUser = userRepository.findById(invitedUserId)
                    .orElse(null);
            if (invitedUser == null) {
                skipped.add(GroupInvitationSkipResponse.builder()
                        .userId(invitedUserId)
                        .reason("User not found")
                        .build());
                continue;
            }

            if (userGroupRepository.existsByUserUserIdAndGroupGroupId(invitedUserId, groupId)) {
                skipped.add(GroupInvitationSkipResponse.builder()
                        .userId(invitedUserId)
                        .reason("User is already a member")
                        .build());
                continue;
            }

            if (groupInvitationRepository.existsByGroupGroupIdAndInvitedUserUserIdAndStatus(
                    groupId, invitedUserId, GroupInvitationStatus.PENDING)) {
                skipped.add(GroupInvitationSkipResponse.builder()
                        .userId(invitedUserId)
                        .reason("Invitation already pending")
                        .build());
                continue;
            }

            GroupInvitation invitation = new GroupInvitation();
            invitation.setGroup(group);
            invitation.setInviterUser(inviter);
            invitation.setInvitedUser(invitedUser);
            invitation.setStatus(GroupInvitationStatus.PENDING);
            invitation.setInvitedAt(LocalDateTime.now());
            invitation = groupInvitationRepository.save(invitation);

            activityLogService.record(
                    group.getGroupId(),
                    inviterUserId,
                    "INVITATION_SENT",
                    null,
                    null,
                    invitedUser.getUserId(),
                    invitedUser.getName(),
                    invitation.getInvitationId(),
                    false
            );

            invitations.add(toInvitationResponse(invitation));
        }

        return GroupInvitationBatchResponse.builder()
                .groupId(groupId)
                .invitedCount(invitations.size())
                .skippedCount(skipped.size())
                .invitations(invitations)
                .skipped(skipped)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupInvitationResponse> getPendingInvitations(Long groupId) {
        Long userId = resolveCurrentUserId();

        List<GroupInvitation> invitations;
        if (groupId != null) {
            ensureAdminMember(groupId, userId);
            invitations = groupInvitationRepository.findByGroupGroupIdAndStatusOrderByInvitedAtDesc(groupId, GroupInvitationStatus.PENDING);
        } else {
            invitations = groupInvitationRepository.findByInvitedUserUserIdAndStatusOrderByInvitedAtDesc(userId, GroupInvitationStatus.PENDING);
        }

         return invitations
                .stream()
                .map(this::toInvitationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupInvitationResponse acceptInvitation(Long invitationId) {
        Long userId = resolveCurrentUserId();
        GroupInvitation invitation = groupInvitationRepository.findByInvitationIdAndInvitedUserUserId(invitationId, userId)
                .orElseThrow(() -> new NotFoundException("GroupInvitation", "id", invitationId.toString()));

        if (invitation.getStatus() == GroupInvitationStatus.ACCEPTED) {
            return toInvitationResponse(invitation);
        }
        if (invitation.getStatus() == GroupInvitationStatus.REJECTED) {
            throw new APIException(HttpStatus.CONFLICT, "Invitation has already been rejected");
        }

        Long groupId = invitation.getGroup().getGroupId();
        if (!userGroupRepository.existsByUserUserIdAndGroupGroupId(userId, groupId)) {
            Permission memberPermission = permissionRepository.findByPermissionName("MEMBER")
                    .orElseThrow(() -> new NotFoundException("Permission", "name", "MEMBER"));

            UserGroup userGroup = new UserGroup();
            userGroup.setUser(invitation.getInvitedUser());
            userGroup.setGroup(invitation.getGroup());
            userGroup.setPermission(memberPermission);
            userGroupRepository.save(userGroup);
        }

        invitation.setStatus(GroupInvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitation = groupInvitationRepository.save(invitation);

        activityLogService.updateInvitationStatus(
                groupId,
                userId,
                "INVITATION_ACCEPTED",
                invitation.getInvitedUser().getUserId(),
                invitation.getInvitedUser().getName(),
                invitation.getInvitationId(),
                false
        );

        return toInvitationResponse(invitation);
    }

    @Override
    @Transactional
    public GroupInvitationResponse rejectInvitation(Long invitationId) {
        Long userId = resolveCurrentUserId();
        GroupInvitation invitation = groupInvitationRepository.findByInvitationIdAndInvitedUserUserId(invitationId, userId)
                .orElseThrow(() -> new NotFoundException("GroupInvitation", "id", invitationId.toString()));

        if (invitation.getStatus() == GroupInvitationStatus.REJECTED) {
            return toInvitationResponse(invitation);
        }
        if (invitation.getStatus() == GroupInvitationStatus.ACCEPTED) {
            throw new APIException(HttpStatus.CONFLICT, "Invitation has already been accepted");
        }

        invitation.setStatus(GroupInvitationStatus.REJECTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitation = groupInvitationRepository.save(invitation);

        activityLogService.updateInvitationStatus(
                invitation.getGroup().getGroupId(),
                userId,
                "INVITATION_REJECTED",
                invitation.getInvitedUser().getUserId(),
                invitation.getInvitedUser().getName(),
                invitation.getInvitationId(),
                false
        );

        return toInvitationResponse(invitation);
    }


    private void ensureAdminMember(Long groupId, Long userId) {
        UserGroup userGroup = userGroupRepository.findByUserUserIdAndGroupGroupId(userId, groupId)
                .orElseThrow(() -> new ForbiddenException("You do not have permission to manage invitations for this group"));

        if (userGroup.getPermission() == null
                || userGroup.getPermission().getPermissionName() == null
                || !userGroup.getPermission().getPermissionName().equalsIgnoreCase("ADMIN")) {
            throw new ForbiddenException("You do not have permission to manage invitations for this group");
        }
    }

    private GroupInvitationResponse toInvitationResponse(GroupInvitation invitation) {
        return GroupInvitationResponse.builder()
                .invitationId(invitation.getInvitationId())
                .groupId(invitation.getGroup() == null ? null : invitation.getGroup().getGroupId())
                .groupName(invitation.getGroup() == null ? null : invitation.getGroup().getGroupName())
                .inviterUserId(invitation.getInviterUser() == null ? null : invitation.getInviterUser().getUserId())
                .inviterName(invitation.getInviterUser() == null ? null : invitation.getInviterUser().getName())
                .invitedUserId(invitation.getInvitedUser() == null ? null : invitation.getInvitedUser().getUserId())
                .invitedUserName(invitation.getInvitedUser() == null ? null : invitation.getInvitedUser().getName())
                .status(invitation.getStatus())
                .invitedAt(invitation.getInvitedAt())
                .respondedAt(invitation.getRespondedAt())
                .build();
    }

    private String buildGroupUpdateDetail(Group currentGroup, GroupRequest request) {
        List<String> changes = new ArrayList<>();
        appendChange(changes, "groupName", currentGroup.getGroupName(), request.getGroupName());
        appendChange(changes, "description", currentGroup.getDescription(), request.getDescription());
        appendChange(changes, "icon", currentGroup.getIcon(), request.getIcon());
        appendChange(changes, "color", currentGroup.getColor(), request.getColor());
        appendChange(changes, "bg", currentGroup.getBg(), request.getBg());

        if (changes.isEmpty()) {
            return "No field changes";
        }
        return String.join("; ", changes);
    }

    private void appendChange(List<String> changes, String field, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(field + ": " + safeText(oldValue) + " -> " + safeText(newValue));
        }
    }

    private String safeText(Object value) {
        return value == null ? "null" : value.toString();
    }

}
