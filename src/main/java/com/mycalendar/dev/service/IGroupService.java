package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.GroupAddMemberRequest;
import com.mycalendar.dev.payload.request.GroupInviteUsersRequest;
import com.mycalendar.dev.payload.request.GroupRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.GroupInvitationBatchResponse;
import com.mycalendar.dev.payload.response.GroupInvitationResponse;
import com.mycalendar.dev.payload.response.GroupResponse;
import com.mycalendar.dev.payload.response.GroupUserResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;

import java.util.List;

public interface IGroupService {
    GroupResponse create(GroupRequest request);

    GroupResponse update(GroupRequest request, Long id);

    GroupResponse getGroupById(Long groupId);

    PaginationResponse<GroupResponse> getAllGroup(PaginationRequest request);

    void addMemberToGroup(GroupAddMemberRequest request);

    List<GroupResponse> getGroupsByUserId(Long userId);

    List<GroupUserResponse> getUsersByGroupId(Long groupId);

    void removeMember(Long groupId, Long userId);

    void deleteGroup(Long id, Long requestUserId);

    GroupResponse joinByCode(String inviteCode);

    GroupInvitationBatchResponse inviteUsers(Long groupId, GroupInviteUsersRequest request);

    List<GroupInvitationResponse> getPendingInvitations(Long groupId);

    GroupInvitationResponse acceptInvitation(Long invitationId);

    GroupInvitationResponse rejectInvitation(Long invitationId);
}
