package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class RevokeAccessTokenRequest {
    private String accessToken;
}