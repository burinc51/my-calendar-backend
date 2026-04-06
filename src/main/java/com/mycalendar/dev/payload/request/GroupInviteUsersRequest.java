package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GroupInviteUsersRequest {

    @NotEmpty(message = "userIds is required")
    private List<@NotNull(message = "userId must not be null") Long> userIds;
}

