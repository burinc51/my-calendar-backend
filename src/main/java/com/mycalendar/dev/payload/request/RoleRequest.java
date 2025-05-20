package com.mycalendar.dev.payload.request;

import com.mycalendar.dev.enums.RoleName;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class RoleRequest {
    @Enumerated(EnumType.STRING)
    private RoleName name;
}
