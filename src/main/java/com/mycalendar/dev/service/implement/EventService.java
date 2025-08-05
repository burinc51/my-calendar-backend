package com.mycalendar.dev.service.implement;


import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.enums.FileType;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.EventResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IEventService;
import com.mycalendar.dev.util.EntityMapper;
import com.mycalendar.dev.util.FileHandler;
import com.mycalendar.dev.util.GenericSpecification;
import io.micrometer.common.lang.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService implements IEventService {

    @Value("${spring.application.base-url}")
    private String baseUrl;

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public EventResponse saveEvent(EventRequest eventRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));
        Event event = EntityMapper.mapToEntity(eventRequest, Event.class);
        event.setUser(user);
        event = eventRepository.save(event);

        return EntityMapper.mapToEntity(event, EventResponse.class);
    }

    public EventResponse saveOrUpdate(EventRequest eventRequest, Long eventId, Long userId, @Nullable MultipartFile file) {

        // ตรวจสอบ user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

        Event event;
        if (eventId != null) {
            // อัปเดต Event เดิม
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Event", "id", eventId.toString()));
        } else {
            // สร้าง Event ใหม่
            event = EntityMapper.mapToEntity(eventRequest, Event.class);
            event.setEventId(null);
            event.setUser(user);
            event = eventRepository.save(event); // บันทึกเพื่อให้ได้ eventId
        }

        // จัดการไฟล์ (ถ้ามี)
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }
            String fileUpload = FileHandler.upload(file, FileType.IMAGES, event.getImageUrl(), "/event/" + event.getEventId());
            if (fileUpload != null) {
                event.setImageUrl(fileUpload);
            } else {
                throw new RuntimeException("Failed to upload file");
            }
        }

        // บันทึก event อีกครั้งถ้ามีการเปลี่ยนแปลง
        event = eventRepository.save(event);

        // แปลงเป็น EventResponse และเพิ่ม baseUrl
        EventResponse response = EntityMapper.mapToEntity(event, EventResponse.class);
        if (response.getImageUrl() != null) {
            response.setImageUrl(baseUrl + response.getImageUrl());
        }
        return response;
    }

    @Override
    public EventResponse getEventById(Long noteId) {
        Event event = eventRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Event", "id", noteId.toString()));
        return EntityMapper.mapToEntity(event, EventResponse.class);
    }

    @Override
    public void delete(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        eventRepository.delete(event);
    }

    @Override
    public PaginationResponse getAllEvents(PaginationRequest request) {
        Map<String, Object> keywordMap = request.getFilter();
        List<String> fields = new ArrayList<>(keywordMap.keySet());
        Specification<Event> spec = new GenericSpecification<Event>().getSpecification(keywordMap, fields);

        Page<Event> pages = eventRepository.findAll(spec, request.getPageRequest());

        List<EventResponse> content = pages.stream()
                .map(event -> EntityMapper.mapToEntity(event, EventResponse.class))
                .collect(Collectors.toList());

        return new PaginationResponse(content, request.getPageNumber(), request.getPageSize(), pages.getTotalElements(), pages.getTotalPages(), pages.isLast());
    }

}
