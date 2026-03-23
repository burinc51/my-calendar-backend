package com.mycalendar.dev.payload.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GroupResponse(
        Long groupId,
        String groupName,
        String icon,
        String color,
        String bg,
        List<GroupMemberResponse> members
) {
}
