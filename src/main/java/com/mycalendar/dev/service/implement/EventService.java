package com.mycalendar.dev.service.implement;


import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.enums.FileType;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.EventResponse;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.GroupMemberRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IEventService;
import com.mycalendar.dev.util.EntityMapper;
import com.mycalendar.dev.util.FileHandler;
import com.mycalendar.dev.util.GenericSpecification;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService implements IEventService {

    private final GroupRepository groupRepository;
    @Value("${spring.application.base-url}")
    private String baseUrl;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    public EventResponse saveEvent(EventRequest eventRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));
        Event event = EntityMapper.mapToEntity(eventRequest, Event.class);
        event.setUser(user);
        event = eventRepository.save(event);

        return EntityMapper.mapToEntity(event, EventResponse.class);
    }

    @Transactional
    public EventResponse saveOrUpdate(EventRequest eventRequest, Long eventId, Long userId, @Nullable MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

        Group group = groupRepository.findById(eventRequest.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group", "id", eventRequest.getGroupId().toString()));

        Event event = (eventId != null)
                ? eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", "id", eventId.toString()))
                : EntityMapper.mapToEntity(eventRequest, Event.class);

        if (eventId == null) {
            event.setEventId(null);
            event.setUser(user);
            event.setGroup(group);
        }

        if (event.getUsers() == null) {
            event.setUsers(new HashSet<>());
        }
        if (eventRequest.getAssigneeIds() != null && !eventRequest.getAssigneeIds().isEmpty()) {
            List<Long> invalidIds = eventRequest.getAssigneeIds().stream()
                    .filter(assigneeId -> !groupMemberRepository.existsByGroupGroupIdAndUserId(group.getGroupId(), assigneeId))
                    .toList();
            if (!invalidIds.isEmpty()) {
                throw new IllegalArgumentException("Users with IDs " + invalidIds + " are not members of the group");
            }

            Set<User> assignees = new HashSet<>(userRepository.findAllById(eventRequest.getAssigneeIds()));
            if (assignees.size() != eventRequest.getAssigneeIds().size()) {
                throw new NotFoundException("User", "ids", eventRequest.getAssigneeIds().stream()
                        .filter(id -> assignees.stream().noneMatch(users -> users.getId().equals(id)))
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")));
            }
            event.getUsers().clear();
            event.getUsers().addAll(assignees);
        } else {
            event.getUsers().clear();
        }

        event = eventRepository.save(event);

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }
            String fileUpload = FileHandler.upload(file, FileType.IMAGES, event.getImageUrl(), "/event/" + event.getEventId());
            if (fileUpload == null) {
                throw new RuntimeException("Failed to upload file");
            }
            event.setImageUrl(fileUpload);
        }

        event = eventRepository.save(event);

        EventResponse response = EntityMapper.mapToEntity(event, EventResponse.class);
        if (response.getImageUrl() != null) {
            response.setImageUrl(baseUrl + response.getImageUrl());
        }

        response.setAssignees(event.getUsers().stream()
                .map(users -> {
                    EventResponse.AssigneeDTO assigneeDTO = new EventResponse.AssigneeDTO();
                    assigneeDTO.setUserId(users.getId());
                    assigneeDTO.setUserName(users.getName());
                    return assigneeDTO;
                })
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    public EventResponse getEventById(Long noteId) {
        Event event = eventRepository.findById(noteId)
                .orElseThrow(() -> new NotFoundException("Event", "id", noteId.toString()));

        EventResponse response = EntityMapper.mapToEntity(event, EventResponse.class);
        response.setAssignees(event.getUsers().stream()
                .map(users -> {
                    EventResponse.AssigneeDTO assigneeDTO = new EventResponse.AssigneeDTO();
                    assigneeDTO.setUserId(users.getId());
                    assigneeDTO.setUserName(users.getName());
                    return assigneeDTO;
                })
                .collect(Collectors.toList()));
        return response;
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
