package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.entity.UserGroup;
import com.mycalendar.dev.exception.ForbiddenException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.mapper.GroupMapper;
import com.mycalendar.dev.payload.request.GroupAddMemberRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupMemberResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.projection.GroupProjection;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.PermissionRepository;
import com.mycalendar.dev.repository.UserGroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IActivityLogService;
import com.mycalendar.dev.service.IGroupService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService implements IGroupService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final IActivityLogService activityLogService;

    public GroupService(UserRepository userRepository, PermissionRepository permissionRepository,
                        GroupRepository groupRepository, UserGroupRepository userGroupRepository,
                        IActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public void create(GroupRequest request) {
        User creator = userRepository.findById(request.getCreatorUserId())
                .orElseThrow(() -> new NotFoundException("User", "id", request.getCreatorUserId().toString()));

        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());

        groupRepository.save(group);

        Permission adminPermission = permissionRepository.findByPermissionName("ADMIN")
                .orElseThrow(() -> new NotFoundException("Permission", "name", "ADMIN"));

        UserGroup userGroup = new UserGroup();
        userGroup.setUser(creator);
        userGroup.setGroup(group);
        userGroup.setPermission(adminPermission);
        userGroupRepository.save(userGroup);

        // Record activity: group created
        activityLogService.record(
                group.getGroupId(),
                creator.getUserId(),
                "GROUP_CREATED",
                null, null,
                null, null,
                false
        );
    }

    @Override
    public GroupResponse update(GroupRequest request, Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Group", "groupId", id.toString()));

        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());

        groupRepository.save(group);

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

        List<GroupResponse> groups = pages.getContent().stream().map(GroupMapper::mapToDto).toList();

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
    public List<GroupMemberResponse> getUsersByGroupId(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new NotFoundException("Group", "id", groupId.toString());
        }

        return userGroupRepository.findMembersByGroupId(groupId).stream()
                .map(v -> GroupMemberResponse.builder()
                        .userId(v.getUser().getUserId())
                        .name(v.getUser().getName())
                        .username(v.getUser().getUsername())
                        .role(v.getPermission().getPermissionName())
                        .build())
                .toList();
    }

    @Override
    public void removeMember(Long groupId, Long userId) {
        UserGroup userGroup = userGroupRepository.findByUserUserIdAndGroupGroupId(userId, groupId)
                .orElseThrow(() -> new NotFoundException("UserGroup", "user/group", userId + "/" + groupId));

        User removedUser = userGroup.getUser();
        userGroupRepository.delete(userGroup);

        // Record activity: member removed
        activityLogService.record(
                groupId,
                userId,
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

}
