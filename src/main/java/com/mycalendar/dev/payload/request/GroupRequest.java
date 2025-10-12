package com.mycalendar.dev.payload.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GroupRequest {
    private String groupName;
    private String description;
    private Long creatorUserId; // user ที่สร้างกลุ่ม
}
