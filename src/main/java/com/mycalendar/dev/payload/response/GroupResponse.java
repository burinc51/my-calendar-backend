package com.mycalendar.dev.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class GroupResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private String imageUrl;
    private Long creatorByUserId;
    private List<GroupMemberResponse> members;

}
