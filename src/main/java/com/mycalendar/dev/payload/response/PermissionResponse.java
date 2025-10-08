package com.mycalendar.dev.payload.response;

import lombok.Builder;

@Builder
public record PermissionResponse(
        Long permissionId,
        String permissionName
) {
}
