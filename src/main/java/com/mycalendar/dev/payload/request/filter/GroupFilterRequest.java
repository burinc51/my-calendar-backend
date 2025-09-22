package com.mycalendar.dev.payload.request.filter;

import lombok.Data;

@Data
public class GroupFilterRequest {
    private Long groupId;
    private String groupName;
    private String description;
    private Long creatorId;
}
