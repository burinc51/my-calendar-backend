package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findAll(Specification<Group> spec, Pageable pageable);
}