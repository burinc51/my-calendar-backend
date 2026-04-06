package com.mycalendar.dev.mapper;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.GroupUserResponse;
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
                .icon(group.getIcon())
                .color(group.getColor())
                .bg(group.getBg())
                .members(group.getUserGroups().stream().map(
                        v -> GroupUserResponse.builder()
                                .userId(v.getUser().getUserId())
                                .username(v.getUser().getUsername())
                                .name(v.getUser().getName())
                                .imageUrl(v.getUser().getPictureUrl())
                                .build()
                ).toList())
                .build();
    }

    public static List<GroupResponse> mapToDto(List<GroupProjection> projections) {
        Map<Long, GroupResponse> grouped = new LinkedHashMap<>();

        for (GroupProjection row : projections) {
            Long groupId = row.getGroupId();

            GroupResponse response = grouped.computeIfAbsent(groupId, id ->
                    GroupResponse.builder()
                            .groupId(row.getGroupId())
                            .groupName(row.getGroupName())
                            .icon(row.getIcon())
                            .color(row.getColor())
                            .bg(row.getBg())
                            .members(new ArrayList<>())
                            .build()
            );

            response.members().add(
                    GroupUserResponse.builder()
                            .userId(row.getUserId())
                            .username(row.getUsername())
                            .name(row.getName())
                            .imageUrl(row.getPictureUrl())
                            .build()
            );
        }

        return new ArrayList<>(grouped.values());
    }
}
