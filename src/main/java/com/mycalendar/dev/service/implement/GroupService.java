package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.GroupAddMemberRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupMemberResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.PermissionRepository;
import com.mycalendar.dev.repository.UserRepository;
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

    public GroupService(UserRepository userRepository, PermissionRepository permissionRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public void create(GroupRequest request) {
        // 1) find user creator
        User creator = userRepository.findById(request.getCreatorUserId())
                .orElseThrow(() -> new NotFoundException("User", "id", request.getCreatorUserId().toString()));

        // 2) find permission ADMIN
        Permission adminPermission = permissionRepository.findByPermissionName("ADMIN")
                .orElseThrow(() -> new NotFoundException("Permission", "name", "ADMIN"));

        // 3) create a new group
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());

        // 4) Associate Group ↔ User relationship
        group.getUsers().add(creator);
        creator.getGroups().add(group); // ✅ sync both sides

        // 5) Associate User ↔ Permission
        creator.getPermissions().add(adminPermission);
        adminPermission.getUsers().add(creator);

        // 6) save a group (cascade will help save other relationships)
        groupRepository.save(group);
    }

    @Override
    public PaginationResponse<GroupResponse> getAllGroup(PaginationRequest request) {
        Page<Group> pages = groupRepository.findAll(request.getPageRequest());

        List<GroupResponse> groups = pages.getContent().stream().map(
                value -> GroupResponse.builder()
                        .groupId(value.getGroupId())
                        .groupName(value.getGroupName())
                        .description(value.getDescription())
                        .members(value.getUsers().stream().map(
                                v -> GroupMemberResponse.builder()
                                        .userId(v.getUserId())
                                        .name(v.getName())
                                        .username(v.getUsername())
                                        .role(v.getPermissions().stream().findFirst().map(Permission::getPermissionName).orElse("MEMBER"))
                                        .build()
                        ).toList())
                        .build()
        ).toList();

        return PaginationResponse.<GroupResponse>builder()
                .content(groups)
                .pageNo(pages.getNumber())
                .pageSize(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }

    @Override
    public void addMemberToGroup(GroupAddMemberRequest request) {
        // 1) find a group
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group", "id", request.getGroupId().toString()));

        // 2) find a user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User", "id", request.getUserId().toString()));

        // 3) Check if the user is already in the group
        if (group.getUsers().contains(user)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        // 4) Associate Group ↔ User relationship
        group.getUsers().add(user);
        user.getGroups().add(group);

        // 5) Add permission (Permission)
        String roleName = request.getRole() != null ? request.getRole() : "MEMBER";
        Permission permission = permissionRepository.findByPermissionName(roleName)
                .orElseThrow(() -> new NotFoundException("Permission", "name", roleName));

        user.getPermissions().add(permission);
        permission.getUsers().add(user);

        // 6) Save group
        groupRepository.save(group);
    }

    @Override
    public List<GroupResponse> getGroupsByUserId(Long userId) {
        List<Group> result = groupRepository.findAllByUserId(userId);

        if (result == null) {
            return List.of();
        }

        return result.stream().map(
                value -> GroupResponse.builder()
                        .groupId(value.getGroupId())
                        .groupName(value.getGroupName())
                        .description(value.getDescription())
                        .members(value.getUsers().stream().map(
                                v -> GroupMemberResponse.builder()
                                        .userId(v.getUserId())
                                        .name(v.getName())
                                        .username(v.getUsername())
                                        .role(v.getPermissions().stream().findFirst().map(Permission::getPermissionName).orElse("MEMBER"))
                                        .build()
                        ).toList())
                        .build()
        ).toList();
    }
}
