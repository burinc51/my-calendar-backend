package com.mycalendar.dev.payload.request;

import lombok.Data;

import java.util.List;

@Data
public class RemoveMembersRequest {
    private List<Long> memberIds;
}
