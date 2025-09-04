package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.EventResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.service.IEventService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.mycalendar.dev.helper.ApiDocHelper.CREATE_EVENT_DESCRIPTION;
import static com.mycalendar.dev.helper.ApiDocHelper.CREATE_EVENT_SUMMARY;

@RestController
@RequestMapping("/api/events")
public class EventRestController {
    private final IEventService eventService;

    public EventRestController(IEventService eventService) {
        this.eventService = eventService;
    }

    @Operation(summary = CREATE_EVENT_SUMMARY, description = CREATE_EVENT_DESCRIPTION)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventResponse createEvent(@Valid @RequestPart("body") EventRequest eventRequest, @RequestPart(value = "file") MultipartFile file) throws IllegalAccessException {
        Long userId = eventRequest.getUserId();
        return eventService.saveOrUpdate(eventRequest, null, userId, file);
    }

    @PutMapping("/{eventId}/{userId}")
    public EventResponse createEvent(@RequestBody EventRequest eventRequest, @PathVariable Long eventId, @PathVariable Long userId, MultipartFile file) throws IllegalAccessException {
        return eventService.saveOrUpdate(eventRequest, eventId, userId, file);
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
