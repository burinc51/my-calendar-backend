package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotEmpty(message = "Name should not be null.")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Invalid name format, please use only letters and spaces.")
    private String name;
}