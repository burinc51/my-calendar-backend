package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.SignUpRequest;
import com.mycalendar.dev.payload.request.UserUpdateRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.UserResponse;

public interface IUserService {
    void create(SignUpRequest signUpRequest);

    void update(UserUpdateRequest userRequest, Long id);

    void delete(Long id);

    PaginationResponse getAll(int page, int size, String filter, String sort, String keyword);

    UserResponse getById(Long id);

    void addRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);
}
