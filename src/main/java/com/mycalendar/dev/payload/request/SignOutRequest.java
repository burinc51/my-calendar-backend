package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class SignOutRequest {
    private String accessToken;
    private String refreshToken;
}