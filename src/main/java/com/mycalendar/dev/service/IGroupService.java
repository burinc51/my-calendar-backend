package com.mycalendar.dev.service;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;

import java.util.List;

public interface IGroupService {
    Group createGroup(GroupRequest request, Long userId);

    void addMember(Long groupId, Long memberId, Long userAdminId);

    GroupResponse getGroupById(Long groupId);

    void removeMembers(Long groupId, List<Long> memberIds);

    void delete(Long groupId);

    PaginationResponse getAllGroups(PaginationRequest paginationRequest);

    PaginationResponse getAllGroupByUser(PaginationRequest request, Long userId);
}
