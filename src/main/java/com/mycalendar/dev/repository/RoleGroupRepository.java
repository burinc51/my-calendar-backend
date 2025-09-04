package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.RoleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleGroupRepository extends JpaRepository<RoleGroup, Long> {

    Optional<RoleGroup> findByName(String name);

}
