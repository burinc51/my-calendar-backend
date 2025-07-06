package com.mycalendar.dev.payload.response;

import com.mycalendar.dev.entity.Role;
import lombok.Data;

import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private Set<Role> roles;
    private String imageUrl;
}
