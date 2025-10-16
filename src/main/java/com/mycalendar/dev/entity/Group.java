package com.mycalendar.dev.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
@Getter
@Setter
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @Column(nullable = false)
    private String groupName;

    @Column
    private String description;

    // ✅ ความสัมพันธ์กับ user (ผ่าน UserGroup)
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserGroup> userGroups = new HashSet<>();

    // Group - Event (One-to-Many)
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    // Group - Permission (Many-to-Many)
    @ManyToMany
    @JoinTable(
            name = "group_permission",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
}
