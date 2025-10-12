package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.exception.NotFoundException;
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
        // 1) หา user creator
        User creator = userRepository.findById(request.getCreatorUserId())
                .orElseThrow(() -> new NotFoundException("User", "id", request.getCreatorUserId().toString()));

        // 2) หา permission ADMIN
        Permission adminPermission = permissionRepository.findByPermissionName("ADMIN")
                .orElseThrow(() -> new NotFoundException("Permission", "name", "ADMIN"));

        // 3) สร้าง group ใหม่
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());

        // 4) ผูกความสัมพันธ์ Group ↔ User
        group.getUsers().add(creator);
        creator.getGroups().add(group); // ✅ sync สองฝั่ง

        // 6) ผูก User ↔ Permission
        creator.getPermissions().add(adminPermission);
        adminPermission.getUsers().add(creator);

        // 7) save group (cascade จะช่วย save ความสัมพันธ์อื่น ๆ)
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
}
