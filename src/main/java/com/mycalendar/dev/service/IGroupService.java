package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;

public interface IGroupService {
    void create(GroupRequest request);

    PaginationResponse<GroupResponse> getAllGroup(PaginationRequest request);
}
