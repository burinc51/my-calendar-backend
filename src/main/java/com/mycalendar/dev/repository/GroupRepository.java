package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}