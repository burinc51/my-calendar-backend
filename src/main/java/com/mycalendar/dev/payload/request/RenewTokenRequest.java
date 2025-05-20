package com.mycalendar.dev.payload.request;


import lombok.Data;

@Data
public class RenewTokenRequest {
    private String refreshToken;
}
