package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.response.AuthResponse;

public interface IGoogleAuth {
    AuthResponse googleSignIn(String idTokenString);
}
