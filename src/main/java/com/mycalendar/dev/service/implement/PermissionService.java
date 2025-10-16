package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.payload.request.PermissionRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.PaginationWithFilterRequest;
import com.mycalendar.dev.payload.response.PermissionResponse;
import com.mycalendar.dev.projection.PermissionProjection;
import com.mycalendar.dev.repository.PermissionRepository;
import com.mycalendar.dev.service.IPermissionService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mycalendar.dev.util.TypeSafe.validateSortBy;

@Service
public class PermissionService implements IPermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void create(Long permissionId, String permissionName) {
        Permission permission = new Permission();
        permission.setPermissionName(permissionName);
        permissionRepository.save(permission);
    }

    @Override
    public PaginationResponse<PermissionResponse> getAllPermissions(PaginationWithFilterRequest<PermissionRequest> request) {
        validateSortBy(request.getSortBy(), PermissionResponse.class);
        Page<PermissionProjection> pages = permissionRepository.findAllPermission(request.getPageRequest());
        List<PermissionResponse> content = pages.getContent().stream().map(
                value -> PermissionResponse.builder()
                        .permissionId(value.getPermissionId())
                        .permissionName(value.getPermissionName())
                        .build()
        ).toList();

        return PaginationResponse.<PermissionResponse>builder()
                .content(content)
                .pageNo(pages.getNumber())
                .pageSize(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }
}
