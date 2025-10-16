package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.PermissionRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.PaginationWithFilterRequest;
import com.mycalendar.dev.payload.response.PermissionResponse;
import com.mycalendar.dev.service.IPermissionService;
import com.mycalendar.dev.service.implement.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionRestController {

    private final IPermissionService permissionService;

    public PermissionRestController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestParam(required = false) Long permissionId, @RequestParam String permissionName) {
        permissionService.create(permissionId, permissionName);
        return ResponseEntity.ok("Permission created successfully.");
    }

    @PostMapping("/all")
    public PaginationResponse<PermissionResponse> getAllPermission(@RequestBody PaginationWithFilterRequest<PermissionRequest> request) {
        return permissionService.getAllPermissions(request);
    }
}
