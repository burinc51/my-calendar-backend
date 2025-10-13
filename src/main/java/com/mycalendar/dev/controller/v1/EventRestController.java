package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.service.IEventService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/all/{groupId}")
    public PaginationResponse<EventResponse> getAllByGroup(@RequestBody PaginationRequest request, @PathVariable Long groupId) {
        return eventService.getAllEventByGroup(groupId, request);
    }
}
