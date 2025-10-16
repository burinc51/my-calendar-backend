package com.mycalendar.dev.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
public class UserGroupId implements Serializable {
    private Long userId;
    private Long groupId;
}
