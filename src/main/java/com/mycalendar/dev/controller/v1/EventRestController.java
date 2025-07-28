package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.EventResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.service.IEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventRestController {
    private final IEventService eventService;

    public EventRestController(IEventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{userId}")
    public EventResponse createEvent(@RequestBody EventRequest eventRequest, @PathVariable Long userId) {
        return eventService.saveOrUpdate(eventRequest, null, userId);
    }

    @PutMapping("/{eventId}/{userId}")
    public EventResponse createEvent(@RequestBody EventRequest eventRequest, @PathVariable Long eventId, @PathVariable Long userId) {
        return eventService.saveOrUpdate(eventRequest, eventId, userId);
    }

    @GetMapping("/{id}")
    public EventResponse getById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.ok("Note deleted successfully.");
    }

    @PostMapping("/all")
    public PaginationResponse getAllEvents(@RequestBody PaginationRequest paginationRequest) {
        return eventService.getAllEvents(paginationRequest);
    }
}
