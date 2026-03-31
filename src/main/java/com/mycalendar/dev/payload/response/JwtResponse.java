package com.mycalendar.dev.payload.response;

import lombok.Data;

@Data
public class JwtResponse {
    private final String tokenType = "Bearer";
    private String accessToken;
    private Long expiresIn;
    private String refreshToken;
    private Long refreshExpiresIn;
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String pictureUrl;
    private Boolean isAdmin;
}
