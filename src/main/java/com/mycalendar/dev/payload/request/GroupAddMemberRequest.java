package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class GroupAddMemberRequest {
    private Long groupId;
    private Long userId;
    private Long permissionId; // ADMIN, MEMBER
    /** Optional: the user who is performing the add (for activity logging). Defaults to userId if absent. */
    private Long actorId;
}
