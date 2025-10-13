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
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.projection.EventProjection;
import com.mycalendar.dev.repository.EventRepository;
import com.mycalendar.dev.repository.GroupRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IEventService;
import com.mycalendar.dev.util.FileHandler;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EventService implements IEventService {

    @Value("${spring.application.base-url}")
    private String baseUrl;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventService(GroupRepository groupRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponse saveOrUpdate(EventRequest request, @Nullable MultipartFile file) {

        // 1) find a group
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group", "id", request.getGroupId().toString()));

//        // ตรวจสอบสิทธิ์ — เฉพาะ ADMIN เท่านั้นที่สร้าง event ได้
//        Permission adminPermission = permissionRepository.findByPermissionName("ADMIN")
//                .orElseThrow(() -> new NotFoundException("Permission", "name", "ADMIN"));
//        if (!group.getUsers().contains(creator) || !creator.getPermissions().contains(adminPermission)) {
//            throw new ForbiddenException("Only ADMIN can create events in this group");
//        }

        // 2) Find the creator user and check if they are in the group
        User creator = userRepository.findById(request.getCreateById())
                .orElseThrow(() -> new NotFoundException("User", "id", request.getCreateById().toString()));
        if (!group.getUsers().contains(creator)) {
            throw new IllegalArgumentException("Creator user is not a member of the group");
        }

        // 3) Check that all assigned users are actually in the group
        Set<User> participants = new HashSet<>();
        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            participants = new HashSet<>(userRepository.findAllById(request.getAssigneeIds()));
            List<Long> invalidIds = participants.stream()
                    .filter(u -> !group.getUsers().contains(u))
                    .map(User::getUserId)
                    .toList();
            if (!invalidIds.isEmpty()) {
                throw new IllegalArgumentException("Users with IDs " + invalidIds + " are not in the group");
            }
        }

        // 4) Validate dates
        if (request.getEndDate() != null && request.getStartDate() != null &&
                request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // 5) Calculate notification time if not set
        if (request.getRemindBeforeMinutes() != null && request.getNotificationTime() == null && request.getStartDate() != null) {
            request.setNotificationTime(request.getStartDate().minusMinutes(request.getRemindBeforeMinutes()));
        }

        // 6) Create Event
        Event event = new Event();
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
        event.setGroup(group);
        event.setUsers(participants);
        event.setCreateById(creator.getUserId());

        // 7) Upload image (optional)
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

        // 8) Save
        Event saved = eventRepository.save(event);

        // 9) Map Response
        return EventMapper.mapToDto(saved);
    }

    @Override
    public PaginationResponse<EventResponse> getAllEventByGroup(Long groupId, PaginationRequest request) {

        Page<EventProjection> pages = eventRepository.findAllEventSummaryByGroup(groupId, request.getPageRequest());

        List<EventResponse> content = EventMapper.mapToProjection(pages.getContent());

        return PaginationResponse.<EventResponse>builder()
                .content(content)
                .pageNo(pages.getNumber())
                .pageSize(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .last(pages.isLast())
                .build();
    }
}
