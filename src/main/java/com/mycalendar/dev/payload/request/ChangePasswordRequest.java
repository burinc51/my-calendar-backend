package com.mycalendar.dev.payload.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String usernameOrEmail;
    private String oldPassword;
    private String newPassword;
}
