package com.mycalendar.dev.mapper;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.payload.response.GroupMemberResponse;
import com.mycalendar.dev.payload.response.GroupResponse;

public class GroupMapper {

    public GroupMapper() {
        throw new IllegalStateException("Mapper Class");
    }

    public static GroupResponse mapToDto(Group group) {
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .members(group.getUsers().stream().map(
                        v -> GroupMemberResponse.builder()
                                .userId(v.getUserId())
                                .name(v.getName())
                                .username(v.getUsername())
                                .role(v.getPermissions().stream().findFirst().map(Permission::getPermissionName).orElse("MEMBER"))
                                .build()
                ).toList())
                .build();
    }
}
