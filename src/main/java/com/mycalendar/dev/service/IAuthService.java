package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.response.JwtResponse;
import com.mycalendar.dev.payload.response.UserResponse;

public interface IAuthService {
    JwtResponse signIn(String usernameOrEmail, String password);

    JwtResponse refreshAccessToken(String refreshToken);

    void activateAccount(String activateCode);

    void changePassword(String usernameOrEmail, String oldPassword, String password);

    void forgotPassword(String email);

    void resetPassword(String token, String password);

    UserResponse getCurrentUser();
}
