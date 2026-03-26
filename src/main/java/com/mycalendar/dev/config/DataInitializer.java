package com.mycalendar.dev.config;

import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.repository.PermissionRepository;
import com.mycalendar.dev.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public DataInitializer(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        seedPermissions();
        seedRoles();
    }

    private void seedPermissions() {
        List<String> permissionNames = List.of("ADMIN", "MEMBER");

        for (String name : permissionNames) {
            if (permissionRepository.findByPermissionName(name).isEmpty()) {
                Permission permission = new Permission();
                permission.setPermissionName(name);
                permissionRepository.save(permission);
                System.out.println("[DataInitializer] Seeded permission: " + name);
            }
        }
    }

    private void seedRoles() {
        List<String> roleNames = List.of("ADMIN", "USER");

        for (String name : roleNames) {
            if (roleRepository.findByName(name).isEmpty()) {
                Role role = new Role();
                role.setName(name);
                role.setActive(true);
                roleRepository.save(role);
                System.out.println("[DataInitializer] Seeded role: " + name);
            }
        }
    }
}
