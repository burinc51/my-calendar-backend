package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignUpRequest {
    @NotEmpty(message = "Name should not be null.")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Invalid name format, please use only letters and spaces.")
    private String name;

    @NotEmpty(message = "Username should not be null.")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Invalid username format, please use only letters, digits and underscore.")
    private String username;

    @NotEmpty(message = "Email should not be null.")
    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "Password should not be null.")
    @Size(min = 5, max = 24, message = "Password should contain at least 5 characters, but no more than 24 characters.")
    private String password;

    @NotEmpty(message = "Role should not be null.")
    private Set<String> roles;
}
