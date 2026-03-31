package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password should not be null.")
    @Size(min = 5, max = 24, message = "Password should contain at least 5 characters, but no more than 24 characters.")
    private String password;
}
