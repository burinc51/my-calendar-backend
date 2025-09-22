package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.GroupMember;
import com.mycalendar.dev.entity.RoleGroup;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupMemberResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.repository.GroupMemberRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.RoleGroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IGroupService;
import com.mycalendar.dev.util.EntityMapper;
import com.mycalendar.dev.util.GenericSpecification;
import com.mycalendar.dev.util.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroupService implements IGroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final RoleGroupRepository roleGroupRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository, RoleGroupRepository roleGroupRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.roleGroupRepository = roleGroupRepository;
    }

    @Transactional
    public Group createGroup(GroupRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        RoleGroup roleGroup = roleGroupRepository.findByName("ADMIN").orElseThrow(() -> new RuntimeException("Role not found"));

        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setCreator(user);
        groupRepository.save(group);

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRoleGroup(roleGroup);
        groupMemberRepository.save(groupMember);

        return group;
    }


    @Transactional
    public void addMember(Long groupId, Long memberId, Long userAdminId) {
        User currentUser = userRepository.findById(userAdminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        boolean isGroupAdmin = groupMemberRepository.findByGroupGroupIdAndUserId(groupId, currentUser.getId())
                .map(gm -> "ADMIN".equals(gm.getRoleGroup().getName()))
                .orElse(false);
        if (!isGroupAdmin) {
            throw new RuntimeException("Only group admins can add members");
        }

        boolean isAlreadyMember = groupMemberRepository.findByGroupGroupIdAndUserId(groupId, member.getId())
                .isPresent();
        if (isAlreadyMember) {
            throw new RuntimeException("User is already a member of the group");
        }

        RoleGroup roleGroup = roleGroupRepository.findByName("MEMBER").orElseThrow(() -> new RuntimeException("Role not found"));

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(member);
        groupMember.setRoleGroup(roleGroup);
        groupMemberRepository.save(groupMember);
    }

    @Override
    public GroupResponse getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupResponse response = EntityMapper.mapToEntity(group, GroupResponse.class);

        List<GroupMemberResponse> memberResponses = groupMemberRepository.findByGroupGroupId(groupId)
                .stream()
                .map(gm -> {
                    GroupMemberResponse dto = new GroupMemberResponse();
                    dto.setUserId(gm.getUser().getId());
                    dto.setUsername(gm.getUser().getUsername());
                    dto.setRole(gm.getRoleGroup().getName());
                    return dto;
                })
                .toList();

        response.setCreatorId(group.getCreator().getId());
        response.setMembers(memberResponses);

        return response;
    }

    @Transactional
    public void removeMembers(Long groupId, List<Long> memberIds) {
        Long userAdminId = SecurityUtil.getCurrentUserId();
        if (userAdminId == null) {
            throw new RuntimeException("User not logged in");
        }
        User currentUser = userRepository.findById(userAdminId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isGroupAdmin = groupMemberRepository.findByGroupGroupIdAndUserId(groupId, currentUser.getId())
                .map(gm -> "ADMIN".equals(gm.getRoleGroup().getName()))
                .orElse(false);
        if (!isGroupAdmin) {
            throw new RuntimeException("Only group admins can remove members");
        }

        for (Long memberId : memberIds) {
            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            GroupMember groupMember = groupMemberRepository.findByGroupGroupIdAndUserId(groupId, member.getId())
                    .orElseThrow(() -> new RuntimeException("User is not a member of the group"));

            groupMemberRepository.delete(groupMember);
        }
    }

    @Override
    public void delete(Long groupId) {
        Long userAdminId = SecurityUtil.getCurrentUserId();
        if (userAdminId == null) {
            throw new RuntimeException("User not logged in");
        }
        User currentUser = userRepository.findById(userAdminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        boolean isGroupAdmin = groupMemberRepository.findByGroupGroupIdAndUserId(groupId, currentUser.getId())
                .map(gm -> "ADMIN".equals(gm.getRoleGroup().getName()))
                .orElse(false);
        if (!isGroupAdmin) {
            throw new RuntimeException("Only group admins can delete groups");
        }
        groupMemberRepository.deleteAll(groupMemberRepository.findByGroupGroupId(groupId));
        groupRepository.delete(group);
    }

    @Override
    public PaginationResponse getAllGroups(PaginationRequest request) {
        Map<String, Object> keywordMap = request.getFilter();
        List<String> fields = new ArrayList<>(keywordMap.keySet());
        Specification<Group> spec = new GenericSpecification<Group>().getSpecification(keywordMap, fields);

        Page<Group> pages = groupRepository.findAll(spec, request.getPageRequest());

        List<GroupResponse> content = pages.stream()
                .map(group -> {
                    GroupResponse response = EntityMapper.mapToEntity(group, GroupResponse.class);
                    response.setCreatorId(group.getCreator().getId());
                    List<GroupMemberResponse> memberResponses = groupMemberRepository.findByGroupGroupId(group.getGroupId())
                            .stream()
                            .map(gm -> {
                                GroupMemberResponse dto = new GroupMemberResponse();
                                dto.setUserId(gm.getUser().getId());
                                dto.setUsername(gm.getUser().getUsername());
                                dto.setRole(gm.getRoleGroup().getName());
                                return dto;
                            })
                            .toList();
                    response.setMembers(memberResponses);
                    return response;
                })
                .toList();

        return new PaginationResponse(content, request.getPageNumber(), request.getPageSize(), pages.getTotalElements(), pages.getTotalPages(), pages.isLast());
    }


    @Override
    public PaginationResponse getAllGroupByUser(PaginationRequest request, Long userId) {
        Page<Group> pages = groupRepository.findAllByMemberUserId(userId, request.getPageRequest());

        List<GroupResponse> content = pages.stream()
                .map(group -> {
                    GroupResponse response = EntityMapper.mapToEntity(group, GroupResponse.class);
                    response.setCreatorId(group.getCreator().getId());
                    List<GroupMemberResponse> memberResponses = groupMemberRepository.findByGroupGroupId(group.getGroupId())
                            .stream()
                            .map(gm -> {
                                GroupMemberResponse dto = new GroupMemberResponse();
                                dto.setUserId(gm.getUser().getId());
                                dto.setUsername(gm.getUser().getUsername());
                                dto.setRole(gm.getRoleGroup().getName());
                                return dto;
                            })
                            .toList();
                    response.setMembers(memberResponses);
                    return response;
                })
                .toList();
        return new PaginationResponse(content, request.getPageNumber(), request.getPageSize(), pages.getTotalElements(), pages.getTotalPages(), pages.isLast());
    }
}
