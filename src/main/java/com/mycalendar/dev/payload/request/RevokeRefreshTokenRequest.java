package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class RevokeRefreshTokenRequest {
    private String refreshToken;
}