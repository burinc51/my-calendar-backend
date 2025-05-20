package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.RoleRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.RoleResponse;

public interface IRoleService {
    void create(RoleRequest roleRequest);

    void update(RoleRequest roleRequest, Long id);

    void delete(Long id);

    PaginationResponse getAll(int page, int size, String filter, String sort, String keyword);

    RoleResponse getById(Long id);
}
