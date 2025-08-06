package com.mycalendar.dev.payload.response;

import lombok.Data;

@Data
public class GroupMemberResponse {
    private Long userId;
    private String username;
    private String role;
}
