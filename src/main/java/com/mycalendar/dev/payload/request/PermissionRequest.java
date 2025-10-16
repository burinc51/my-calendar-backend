package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class PermissionRequest {
    private Long permissionId;
    private String permissionName;
}
