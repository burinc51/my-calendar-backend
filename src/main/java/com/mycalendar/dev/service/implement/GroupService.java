package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.exception.ForbiddenException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.mapper.GroupMapper;
import com.mycalendar.dev.payload.request.GroupAddMemberRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
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

        return result.stream().map(GroupMapper::mapToDto).toList();
    }

    @Override
    public GroupResponse removeMember(Long groupId, Long userId) {
        // 1) Find a group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group", "id", groupId.toString()));

        // 2) Find a user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

        // 3) Check if the user is a member of the group
        if (!group.getUsers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        // 4) Remove relationship from both sides
        group.getUsers().remove(user);
        user.getGroups().remove(group);

        // 5) (If there are additional business rules, e.g. remove user's permission in the group)
        // Example: If permission = MEMBER or ADMIN and no longer used
        user.getPermissions().removeIf(p -> p.getPermissionName().equals("MEMBER"));

        // 6) save a group after removal
        groupRepository.save(group);

        // 7) return response
        return GroupMapper.mapToDto(group);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long requestUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group", "id", groupId.toString()));

        User requester = userRepository.findById(requestUserId)
                .orElseThrow(() -> new NotFoundException("User", "id", requestUserId.toString()));

        boolean isAdmin = requester.getPermissions().stream()
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("ADMIN"));

        if (!isAdmin) {
            throw new ForbiddenException("You do not have permission to delete this group");
        }

        groupRepository.delete(group);
    }
}
