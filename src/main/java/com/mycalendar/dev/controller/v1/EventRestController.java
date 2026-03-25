package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.event.EventMonthViewResponse;
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


    /**
     * Create event with optional image file (multipart/form-data).
     * Body JSON goes in the "body" part, image in the "file" part.
     */
    @PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventResponse createEvent(@Valid @RequestPart("body") EventRequest eventRequest,
                                     @RequestPart(value = "file", required = false) MultipartFile file) {
        return eventService.saveOrUpdate(eventRequest, file);
    }

    /**
     * Create event with plain JSON (application/json) — no file upload.
     * Use this when you don't need to attach an image.
     *
     * Example body:
     * {
     *   "title": "Meeting",
     *   "startDate": "2026-03-10T09:00:00",
     *   "endDate":   "2026-03-10T10:00:00",
     *   "groupId": 1,
     *   "createById": 1,
     *   "assigneeIds": [1, 2],
     *   "notificationType": "PUSH",
     *   "remindBeforeValue": 15,
     *   "remindBeforeUnit": "MINUTES",
     *   "repeatType": "NONE"
     * }
     */
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EventResponse createEventJson(@Valid @RequestBody EventRequest eventRequest) {
        return eventService.saveOrUpdate(eventRequest, null);
    }

    /**
     * Update event with optional image file (multipart/form-data).
     */
    @PutMapping(path = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventResponse updateEvent(@Valid @RequestPart("body") EventRequest eventRequest,
                                     @RequestPart(value = "file", required = false) MultipartFile file) {
        return eventService.saveOrUpdate(eventRequest, file);
    }

    /**
     * Update event with plain JSON (application/json) — no file upload.
     * Must include "eventId" in the body to identify which event to update.
     */
    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EventResponse updateEventJson(@Valid @RequestBody EventRequest eventRequest) {
        return eventService.saveOrUpdate(eventRequest, null);
    }

    @GetMapping("/{eventId}")
    public EventResponse getEventById(@PathVariable Long eventId) {
        return eventService.getEventById(eventId);
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
    public void deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
    }

    @PostMapping("/all/{groupId}")
    public PaginationResponse<EventResponse> getAllByGroup(@RequestBody PaginationRequest request, @PathVariable Long groupId) {
        return eventService.getAllEventByGroup(groupId, request);
    }

    @PostMapping("/all")
    public PaginationResponse<EventResponse> getAll(@RequestBody PaginationRequest request) {
        return eventService.getAllEvent(request);
    }

    @GetMapping("/month-view")
    public List<EventMonthViewResponse> getMonthView(@RequestParam String startDate,
                                                     @RequestParam String endDate,
                                                     @RequestParam(required = false) Long groupId) {
        return eventService.getAllEventsByMonthRange(startDate, endDate, groupId);
    }
}
