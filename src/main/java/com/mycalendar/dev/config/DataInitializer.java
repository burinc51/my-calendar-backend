package com.mycalendar.dev.config;

import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.repository.PermissionRepository;
import com.mycalendar.dev.repository.RoleRepository;
import com.mycalendar.dev.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "123456";
    private static final String ADMIN_NAME = "Admin";
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    public DataInitializer(PermissionRepository permissionRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedPermissions();
        seedRoles();
        seedAdminUser();
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

    private void seedAdminUser() {
        String safeUsername = ADMIN_USERNAME.trim();
        String safeEmail = ADMIN_EMAIL.trim().toLowerCase(Locale.ROOT);
        String safeName = ADMIN_NAME.trim();

        if (safeUsername.isEmpty() || safeEmail.isEmpty()) {
            System.out.println("[DataInitializer] Skip admin bootstrap because username/email is blank.");
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found during bootstrap"));

        Optional<User> userByUsername = userRepository.findByUsernameIncludingInactive(safeUsername);
        Optional<User> userByEmail = userRepository.findByEmailIncludingInactive(safeEmail);
        User adminUser = userByUsername.orElseGet(() -> userByEmail.orElse(null));

        if (adminUser == null) {
            User newAdmin = new User();
            newAdmin.setUsername(safeUsername);
            newAdmin.setEmail(safeEmail);
            newAdmin.setName(safeName);
            newAdmin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            newAdmin.setActive(true);
            newAdmin.setRoles(new HashSet<>(Set.of(adminRole)));
            userRepository.save(newAdmin);
            System.out.println("[DataInitializer] Seeded admin user: " + safeUsername);
            return;
        }

        if (!adminUser.isActive()) {
            adminUser.setActive(true);
        }
        if (adminUser.getRoles() == null) {
            adminUser.setRoles(new HashSet<>());
        }
        if (adminUser.getRoles().stream().noneMatch(role -> "ADMIN".equals(role.getName()))) {
            adminUser.getRoles().add(adminRole);
            userRepository.save(adminUser);
            System.out.println("[DataInitializer] Assigned ADMIN role to existing user: " + adminUser.getUsername());
            return;
        }

        userRepository.save(adminUser);
        System.out.println("[DataInitializer] Admin user already exists: " + adminUser.getUsername());
    }
}
