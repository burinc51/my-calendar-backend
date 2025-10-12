package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class GroupAddMemberRequest {
    private Long groupId;
    private Long userId;
    private String role; // ADMIN, MEMBER
}
