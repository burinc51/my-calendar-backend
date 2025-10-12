package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Permission;
import com.mycalendar.dev.projection.PermissionProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("""
                SELECT
                    p.permissionId AS permissionId,
                    p.permissionName AS permissionName
                FROM Permission p
            """)
    Page<PermissionProjection> findAllPermission(Pageable pageable);

    Optional<Permission> findByPermissionName(String permissionName);
}
