package com.mycalendar.dev.payload.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddMemberRequest {
    private Long memberId;
    private Long userAdminId;
}