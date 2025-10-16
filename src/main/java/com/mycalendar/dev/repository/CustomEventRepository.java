package com.mycalendar.dev.repository;

import com.mycalendar.dev.payload.response.event.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomEventRepository {

    Page<EventResponse> findAllEventByGroup(Long groupId, Pageable pageable);
}
