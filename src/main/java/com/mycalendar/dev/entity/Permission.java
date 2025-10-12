package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Column(nullable = false, unique = true)
    private String permissionName; // e.g. ADMIN, MEMBER

    // Permission - User
    @ManyToMany(mappedBy = "permissions")
    private Set<User> users = new HashSet<>();

    // Permission - Group
    @ManyToMany(mappedBy = "permissions")
    private Set<Group> groups = new HashSet<>();
}

