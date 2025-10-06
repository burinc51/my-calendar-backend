package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Column(nullable = false, unique = true)
    private String permissionName;

    // Role - User
    @ManyToMany
    @JoinTable(
            name = "user_permission",
            joinColumns = @JoinColumn(name = "permission_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    // Role - Group
    @ManyToMany
    @JoinTable(
            name = "group_permission",
            joinColumns = @JoinColumn(name = "permission_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups;

}
