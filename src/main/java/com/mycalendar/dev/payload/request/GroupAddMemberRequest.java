package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class GroupAddMemberRequest {
    private Long groupId;
    private Long userId;
    private Long permissionId; // ADMIN, MEMBER
}
