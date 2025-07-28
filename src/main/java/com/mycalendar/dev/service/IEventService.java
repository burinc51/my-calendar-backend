package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.EventResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;

public interface IEventService {
    EventResponse saveEvent(EventRequest eventRequest, Long userId);

    EventResponse saveOrUpdate(EventRequest eventRequest, Long eventId, Long userId);

    EventResponse getEventById(Long eventId);

    void delete(Long eventId);

    PaginationResponse getAllEvents(PaginationRequest paginationRequest);
}
