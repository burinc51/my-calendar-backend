package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.entity.Group;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.enums.FileType;
import com.mycalendar.dev.exception.APIException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.mapper.EventMapper;
import com.mycalendar.dev.payload.request.EventRequest;
import com.mycalendar.dev.payload.request.PaginationRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.event.EventMonthViewResponse;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.payload.response.event.EventUserSummaryResponse;
import com.mycalendar.dev.projection.EventProjection;
import com.mycalendar.dev.repository.CustomEventRepository;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IActivityLogService;
import com.mycalendar.dev.service.IEventService;
import com.mycalendar.dev.util.FileHandler;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class EventService implements IEventService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CustomEventRepository customEventRepository;
    private final IActivityLogService activityLogService;

    public EventService(GroupRepository groupRepository, UserRepository userRepository,
                        EventRepository eventRepository, CustomEventRepository customEventRepository,
                        IActivityLogService activityLogService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.customEventRepository = customEventRepository;
        this.activityLogService = activityLogService;
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

        // 5) Auto-calculate remindBeforeMinutes from remindBeforeValue + remindBeforeUnit
        //    then derive notificationTime from the total minutes offset.
        if (request.getRemindBeforeValue() != null && request.getRemindBeforeUnit() != null
                && request.getStartDate() != null) {
            int totalMinutes = switch (request.getRemindBeforeUnit().toUpperCase()) {
                case "HOURS" -> request.getRemindBeforeValue() * 60;
                case "DAYS"  -> request.getRemindBeforeValue() * 60 * 24;
                case "WEEKS" -> request.getRemindBeforeValue() * 60 * 24 * 7;
                default      -> request.getRemindBeforeValue(); // MINUTES
            };
            request.setRemindBeforeMinutes(totalMinutes);
            request.setNotificationTime(request.getStartDate().minusMinutes(totalMinutes));
        } else if (request.getRemindBeforeMinutes() != null && request.getStartDate() != null) {
            // Fallback: use remindBeforeMinutes directly
            request.setNotificationTime(request.getStartDate().minusMinutes(request.getRemindBeforeMinutes()));
        }

        // ✅ 6) CREATE or UPDATE
        Event event;
        boolean isCreating = (request.getEventId() == null);
        if (!isCreating) {
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

        // 7) set fields (update in all cases)
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());

        // ✅ Reset notificationSent flag when notificationTime, startDate or remindBeforeMinutes changes
        LocalDateTime oldNotificationTime = event.getNotificationTime();
        LocalDateTime newNotificationTime = request.getNotificationTime();
        boolean notificationChanged = newNotificationTime != null
                && !newNotificationTime.equals(oldNotificationTime);
        if (isCreating || notificationChanged) {
            event.setNotificationSent(false);
        }

        event.setNotificationTime(newNotificationTime);
        event.setNotificationType("PUSH"); // Only PUSH is supported
        event.setRemindBeforeValue(request.getRemindBeforeValue());
        event.setRemindBeforeUnit(request.getRemindBeforeUnit());
        event.setRemindBeforeMinutes(request.getRemindBeforeMinutes());

        // ✅ Repeat fields
        event.setRepeatType(request.getRepeatType());
        event.setRepeatUntil(request.getRepeatUntil());
        event.setRepeatInterval(request.getRepeatInterval() != null ? request.getRepeatInterval() : 1);
        event.setRepeatDays(request.getRepeatDays());

        event.setColor(request.getColor());
        event.setCategory(request.getCategory());
        event.setPriority(request.getPriority());
        event.setPinned(request.getPinned());
        event.setAllDay(request.getAllDay());
        if (isCreating) {
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

        // Record activity log.
        // Skip push notification when the creator is the sole assignee on a new event
        // — there is no one else to notify.
        String actionType = isCreating ? "EVENT_CREATED" : "EVENT_UPDATED";
        boolean onlyCreator = isCreating
                && (participants.isEmpty()
                    || (participants.size() == 1
                        && participants.iterator().next().getUserId().equals(creator.getUserId())));

        activityLogService.record(
                saved.getGroup().getGroupId(),
                creator.getUserId(),
                actionType,
                saved.getEventId(), saved.getTitle(),
                null, null,
                onlyCreator   // skipActivityPush
        );

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
    public void deleteEvent(Long eventId) {
        Long userId = resolveCurrentUserId();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", "id", eventId.toString()));

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "id", userId.toString()));

        Group group = event.getGroup();

        // Only group members can delete an event
        if (group.getUserGroups().stream().noneMatch(ug -> ug.getUser().getUserId().equals(userId))) {
            throw new IllegalArgumentException("User is not a member of the group");
        }

        // Record activity log before deleting (snapshot title while entity still exists)
        activityLogService.record(
                group.getGroupId(),
                userId,
                "EVENT_DELETED",
                event.getEventId(), event.getTitle(),
                null, null,
                false  // always notify group members when an event is deleted
        );

        eventRepository.delete(event);
    }

    @Override
    public PaginationResponse<EventResponse> getAllEventByGroup(Long groupId, PaginationRequest request) {
        Pageable pageable = PageRequest.of(Math.max(0, request.getPageNumber() - 1), request.getPageSize());
        String sortBy = normalizeSortBy(request.getSortBy());
        String sortOrder = normalizeSortOrder(request.getSortOrder());

        Page<EventProjection> pages = eventRepository.findAllByGroupId(groupId, pageable, sortBy, sortOrder);
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
        Pageable pageable = PageRequest.of(Math.max(0, request.getPageNumber() - 1), request.getPageSize());
        String sortBy = normalizeSortBy(request.getSortBy());
        String sortOrder = normalizeSortOrder(request.getSortOrder());

        List<EventProjection> projections = eventRepository.findAllEvents(pageable, sortBy, sortOrder);
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
    public List<EventMonthViewResponse> getAllEventsByMonthRange(String startDate, String endDate, Long groupId) {
        Long userId = resolveCurrentUserId();
        YearMonth startMonth = YearMonth.parse(startDate);
        YearMonth endMonth = YearMonth.parse(endDate);

        LocalDateTime startDateTime = startMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = endMonth.atEndOfMonth().atTime(23, 59, 59);

        List<EventProjection> content = eventRepository.findAllEventsByMonthRange(startDateTime, endDateTime, userId, groupId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        Map<Long, EventMonthViewResponse.EventMonthViewResponseBuilder> eventMap = new LinkedHashMap<>();
        Map<Long, List<EventUserSummaryResponse>> assigneeMap = new LinkedHashMap<>();

        for (EventProjection row : content) {
            EventMonthViewResponse.EventMonthViewResponseBuilder eventBuilder = eventMap.computeIfAbsent(
                    row.getEventId(),
                    ignored -> EventMonthViewResponse.builder()
                            .eventId(row.getEventId())
                            .title(row.getTitle())
                            .startDate(Optional.ofNullable(row.getStartDate()).map(d -> d.format(formatter)).orElse(null))
                            .endDate(Optional.ofNullable(row.getEndDate()).map(d -> d.format(formatter)).orElse(null))
                            .color(row.getColor())
                            .allDay(row.getAllDay())
                            .priority(row.getPriority())
                            .createdBy(EventUserSummaryResponse.builder()
                                    .userId(row.getCreatedByUserId())
                                    .username(row.getCreatedByUsername())
                                    .name(row.getCreatedByName())
                                    .imageUrl(row.getCreatedByImageUrl())
                                    .build())
            );

            List<EventUserSummaryResponse> assignees = assigneeMap.computeIfAbsent(row.getEventId(), ignored -> new ArrayList<>());
            if (row.getUserId() != null && assignees.stream().noneMatch(v -> v.userId().equals(row.getUserId()))) {
                assignees.add(EventUserSummaryResponse.builder()
                        .userId(row.getUserId())
                        .username(row.getUsername())
                        .name(row.getName())
                        .imageUrl(row.getUserImageUrl())
                        .build());
            }

            eventBuilder.assignees(assignees);
        }

        return eventMap.values().stream().map(EventMonthViewResponse.EventMonthViewResponseBuilder::build).toList();
    }

    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new APIException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        Long userId = userRepository.findIdByUsername(username);
        if (userId == null) {
            throw new NotFoundException("User", "username", username);
        }

        return userId;
    }

    private String normalizeSortBy(String rawSortBy) {
        if (rawSortBy == null || rawSortBy.isBlank()) {
            return "eventId";
        }

        return switch (rawSortBy.trim().toLowerCase(Locale.ROOT)) {
            case "id", "eventid", "event_id" -> "eventId";
            case "startdate", "start_date" -> "startDate";
            case "enddate", "end_date" -> "endDate";
            case "title" -> "title";
            default -> "eventId";
        };
    }

    private String normalizeSortOrder(String rawSortOrder) {
        if (rawSortOrder == null || rawSortOrder.isBlank()) {
            return "DESC";
        }

        return rawSortOrder.trim().equalsIgnoreCase("ASC") ? "ASC" : "DESC";
    }
}
