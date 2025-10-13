package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
