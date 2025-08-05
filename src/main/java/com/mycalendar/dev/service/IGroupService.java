package com.mycalendar.dev.service;

import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.payload.request.GroupRequest;

public interface IGroupService {
    Group createGroup(GroupRequest request, Long userId);

    void addMember(Long groupId, Long memberId, Long userId);
}
