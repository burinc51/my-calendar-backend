package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Pattern(regexp = "^[a-zA-Z\\s\\u0E00-\\u0E7F]*$", message = "Invalid name format, please use only letters and spaces.")
    private String name;

    private String pictureUrl;
}