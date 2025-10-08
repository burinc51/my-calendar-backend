package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.PermissionRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.PaginationWithFilterRequest;
import com.mycalendar.dev.payload.response.PermissionResponse;

public interface IPermissionService {
    void create(Long permissionId, String permissionName);

    PaginationResponse<PermissionResponse> getAllPermissions(PaginationWithFilterRequest<PermissionRequest> request);
}
