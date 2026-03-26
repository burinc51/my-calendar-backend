package com.mycalendar.dev.mapper;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.payload.response.GroupMemberResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.GroupUserResponse;
import com.mycalendar.dev.projection.GroupProjection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

public class GroupMapper {

    public GroupMapper() {
        throw new IllegalStateException("Mapper Class");
    }

    private static final Map<Long, String> USER_AVATAR_COLORS = new HashMap<>();

    static {
        USER_AVATAR_COLORS.put(1L, "#c084fc");
        USER_AVATAR_COLORS.put(2L, "#818cf8");
        USER_AVATAR_COLORS.put(3L, "#14b8a6");
        USER_AVATAR_COLORS.put(4L, "#f97316");
        USER_AVATAR_COLORS.put(5L, "#ec4899");
    }

    private static String getInitialText(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return name.substring(0, 1).toUpperCase();
    }

    private static String getAvatarColor(Long userId) {
        return USER_AVATAR_COLORS.getOrDefault(userId, "#94a3b8");
    }

    public static GroupResponse mapToDto(Group group) {
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .icon(group.getIcon())
                .color(group.getColor())
                .bg(group.getBg())
                .inviteCode(group.getInviteCode())
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

            // If this group does not exist yet → create a new one
            GroupResponse response = grouped.computeIfAbsent(groupId, id ->
                    GroupResponse.builder()
                            .groupId(row.getGroupId())
                            .groupName(row.getGroupName())
                            .icon(row.getIcon())
                            .color(row.getColor())
                            .bg(row.getBg())
                            .inviteCode(row.getInviteCode())
                            .members(new ArrayList<>())
                            .build()
            );

            // Add member to group
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
