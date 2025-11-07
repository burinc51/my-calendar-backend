package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.enums.FileType;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.mapper.EventMapper;
import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.event.EventMonthViewResponse;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.projection.EventProjection;
import com.mycalendar.dev.repository.CustomEventRepository;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IEventService;
import com.mycalendar.dev.util.FileHandler;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EventService implements IEventService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CustomEventRepository customEventRepository;

    public EventService(GroupRepository groupRepository, UserRepository userRepository, EventRepository eventRepository, CustomEventRepository customEventRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.customEventRepository = customEventRepository;
    }

    @Transactional
    public EventResponse saveOrUpdate(EventRequest request, @Nullable MultipartFile file) {

        // 1) find a group
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group", "id", request.getGroupId().toString()));

        // 2) find creator
        User creator = userRepository.findById(request.getCreateById())
                .orElseThrow(() -> new NotFoundException("User", "id", request.getCreateById().toString()));

        boolean isMember = group.getUserGroups().stream()
                .anyMatch(ug -> ug.getUser().getUserId().equals(creator.getUserId()));
        if (!isMember) {
            throw new IllegalArgumentException("Creator user is not a member of the group");
        }

        // 3) find assignees
        Set<User> participants = new HashSet<>();
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            participants = new HashSet<>(userRepository.findAllById(request.getAssigneeIds()));
            List<Long> invalidIds = participants.stream()
                    .map(User::getUserId)
                    .filter(userId -> group.getUserGroups().stream()
                            .noneMatch(ug -> ug.getUser().getUserId().equals(userId)))
                    .toList();
            if (!invalidIds.isEmpty()) {
                throw new IllegalArgumentException("Users with IDs " + invalidIds + " are not in the group");
            }
        }

        // 4) validate dates
        if (request.getEndDate() != null && request.getStartDate() != null &&
                request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // 5) calculate notification time
        if (request.getRemindBeforeMinutes() != null && request.getNotificationTime() == null && request.getStartDate() != null) {
            request.setNotificationTime(request.getStartDate().minusMinutes(request.getRemindBeforeMinutes()));
        }

        // ✅ 6) CREATE or UPDATE
        Event event;
        if (request.getEventId() != null) {
            // --- UPDATE ---
            event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new NotFoundException("Event", "id", request.getEventId().toString()));

            // Check that the event is in the same group
            if (!event.getGroup().getGroupId().equals(request.getGroupId())) {
                throw new IllegalArgumentException("Cannot move event to another group");
            }

        } else {
            // --- CREATE ---
            event = new Event();
            event.setGroup(group);
            event.setCreateById(creator.getUserId());
        }

        // 7) set fields (update ทุกกรณี)
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setNotificationTime(request.getNotificationTime());
        event.setNotificationType(request.getNotificationType());
        event.setRemindBeforeMinutes(request.getRemindBeforeMinutes());
        event.setRepeatType(request.getRepeatType());
        event.setRepeatUntil(request.getRepeatUntil());
        event.setColor(request.getColor());
        event.setCategory(request.getCategory());
        event.setPriority(request.getPriority());
        event.setPinned(request.getPinned());
        event.setAllDay(request.getAllDay());
        if (request.getEventId() == null) {
            event.setUsers(participants);
        }

        // 8) handle file upload
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }
            String fileUpload = FileHandler.upload(file, FileType.IMAGES, event.getImageUrl(), "/event");
            if (fileUpload == null) {
                throw new RuntimeException("Failed to upload file");
            }
            event.setImageUrl(fileUpload);
        }

        // 9) save and return
        Event saved = eventRepository.save(event);
        return EventMapper.mapToDto(saved);
    }

    @Override
    public EventResponse getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", "id", eventId.toString()));
        return EventMapper.mapToDto(event);
    }

    @Override
    public PaginationResponse<EventResponse> getAllEventByGroup1(Long groupId, PaginationRequest request) {

        Page<EventResponse> pages = customEventRepository.findAllEventByGroup(groupId, request.getPageRequest());

        return PaginationResponse.<EventResponse>builder()
                .content(pages.getContent())
                .pageNo(request.getPageNumber())
                .pageSize(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }

    @Override
    public EventResponse addAssignees(Long eventId, List<Long> userIds) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", "id", eventId.toString()));

        Set<User> existingUsers = event.getUsers();
        List<User> newUsers = userRepository.findAllById(userIds);

        if (newUsers.isEmpty()) {
            throw new NotFoundException("User", "ids", userIds.toString());
        }

        existingUsers.addAll(newUsers);
        eventRepository.save(event);

        return EventMapper.mapToDto(event);
    }

    @Override
    @Transactional
    public EventResponse removeAssignees(Long eventId, List<Long> userIds) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", "id", eventId.toString()));

        event.getUsers().removeIf(u -> userIds.contains(u.getUserId()));
        eventRepository.save(event);

        return EventMapper.mapToDto(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", "id", eventId.toString()));

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

        if (event.getCreateById().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete event created by yourself");
        }

        Group group = event.getGroup();

        if (group.getUserGroups().stream().noneMatch(ug -> ug.getUser().getUserId().equals(userId))) {
            throw new IllegalArgumentException("User is not a member of the group");
        }

        eventRepository.delete(event);
    }

    @Override
    public PaginationResponse<EventResponse> getAllEventByGroup(Long groupId, PaginationRequest request) {
        Pageable pageable = request.getPageRequest();
        Page<EventProjection> pages = eventRepository.findAllByGroupId(groupId, pageable);
        long totalElements = eventRepository.countEventsByGroupId(groupId);

        List<EventResponse> content = EventMapper.mapRowsMergedFromProjection(pages.getContent());

        return PaginationResponse.<EventResponse>builder()
                .content(content)
                .pageNo(request.getPageNumber())
                .pageSize(pages.getSize())
                .totalElements(totalElements)
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }

    @Override
    public PaginationResponse<EventResponse> getAllEvent(PaginationRequest request) {
        Pageable pageable = request.getPageRequest();

        List<EventProjection> projections = eventRepository.findAllEvents(pageable);
        long totalElements = eventRepository.countAllEvents();

        List<EventResponse> content = EventMapper.mapRowsMergedFromProjection(projections);

        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        boolean isLast = (request.getPageNumber() >= totalPages);

        return PaginationResponse.<EventResponse>builder()
                .content(content)
                .pageNo(request.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(isLast)
                .build();
    }

    @Override
    public List<EventMonthViewResponse> getAllEventsByMonthRange(String startDate, String endDate) {
        List<EventProjection> content = eventRepository.findAllEventsByMonthRange(startDate, endDate);

        return content.stream().map(
                v -> EventMonthViewResponse.builder()
                        .eventId(v.getEventId())
                        .title(v.getTitle())
                        .startDate(v.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                        .endDate(v.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                        .color(v.getColor())
                        .allDay(v.getAllDay())
                        .build()
        ).toList();
    }
}
