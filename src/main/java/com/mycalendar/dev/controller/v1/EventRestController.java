package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.service.IEventService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event")
public class EventRestController {

    private final IEventService eventService;

    public EventRestController(IEventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventResponse createEvent(@Valid @RequestPart("body") EventRequest eventRequest, @RequestPart(value = "file") MultipartFile file) {
        return eventService.saveOrUpdate(eventRequest, file);
    }

    @PutMapping(path = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventResponse updateEvent(@Valid @RequestPart("body") EventRequest eventRequest, @RequestParam(required = false) MultipartFile file) {
        return eventService.saveOrUpdate(eventRequest, file);
    }

    @GetMapping("/{eventId}")
    public EventResponse getEventById(@PathVariable Long eventId) {
        return eventService.getEventById(eventId);
    }

    @PostMapping("/all/{groupId}")
    public PaginationResponse<EventResponse> getAllByGroup(@RequestBody PaginationRequest request, @PathVariable Long groupId) {
        return eventService.getAllEventByGroup(groupId, request);
    }

    @PostMapping("/{eventId}/assignees")
    public ResponseEntity<EventResponse> addAssignees(@PathVariable Long eventId, @RequestBody List<Long> userIds) {
        EventResponse response = eventService.addAssignees(eventId, userIds);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}/assignees")
    public ResponseEntity<EventResponse> removeAssignees(@PathVariable Long eventId, @RequestBody List<Long> userIds) {
        EventResponse response = eventService.removeAssignees(eventId, userIds);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable Long eventId, @RequestParam Long requestUserId) {
        eventService.deleteEvent(eventId, requestUserId);
    }
}
