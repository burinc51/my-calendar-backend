package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.GroupMember;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.repository.GroupMemberRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IGroupService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GroupService implements IGroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Group createGroup(GroupRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = new Group();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setCreator(user);
        groupRepository.save(group);

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRole("GROUP_ADMIN");
        groupMemberRepository.save(groupMember);

        return group;
    }


    @Transactional
    public void addMember(Long groupId, Long memberId, Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        boolean isGroupAdmin = groupMemberRepository.findByGroupGroupIdAndUserId(groupId, currentUser.getId())
                .map(gm -> "GROUP_ADMIN".equals(gm.getRole()))
                .orElse(false);
        if (!isGroupAdmin) {
            throw new RuntimeException("Only group admins can add members");
        }

        boolean isAlreadyMember = groupMemberRepository.findByGroupGroupIdAndUserId(groupId, member.getId())
                .isPresent();
        if (isAlreadyMember) {
            throw new RuntimeException("User is already a member of the group");
        }

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(member);
        groupMember.setRole("MEMBER");
        groupMemberRepository.save(groupMember);
    }
}
