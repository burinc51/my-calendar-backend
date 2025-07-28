package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Override
    Optional<Event> findById(Long Long);

    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}
