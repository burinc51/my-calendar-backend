package com.mycalendar.dev.mapper;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.payload.response.GroupMemberResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.projection.GroupProjection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroupMapper {

    public GroupMapper() {
        throw new IllegalStateException("Mapper Class");
    }

    public static GroupResponse mapToDto(Group group) {
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .members(group.getUserGroups().stream().map(
                        v -> GroupMemberResponse.builder()
                                .userId(v.getUser().getUserId())
                                .name(v.getUser().getName())
                                .username(v.getUser().getUsername())
                                .role(v.getPermission().getPermissionName())
                                .build()
                ).toList())
                .build();
    }

    public static List<GroupResponse> mapToDto(List<GroupProjection> projections) {
        Map<Long, GroupResponse> grouped = new LinkedHashMap<>();

        for (GroupProjection row : projections) {
            Long groupId = row.getGroupId();

            // ถ้ายังไม่มี group นี้ → สร้างใหม่
            GroupResponse response = grouped.computeIfAbsent(groupId, id ->
                    GroupResponse.builder()
                            .groupId(row.getGroupId())
                            .groupName(row.getGroupName())
                            .description(row.getDescription())
                            .members(new ArrayList<>())
                            .build()
            );

            // เพิ่ม member เข้า group
            response.members().add(
                    GroupMemberResponse.builder()
                            .userId(row.getUserId())
                            .username(row.getUsername())
                            .name(row.getName())
                            .role(row.getPermissionName())
                            .build()
            );
        }

        return new ArrayList<>(grouped.values());
    }
}
