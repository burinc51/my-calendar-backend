package com.mycalendar.dev.mapper;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.payload.response.event.EventUserResponse;
import com.mycalendar.dev.projection.EventProjection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventMapper {

    public EventMapper() {
        throw new IllegalStateException("Mapper Class");
    }

    public static EventResponse mapToDto(Event event) {
        return EventResponse.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .endDate(event.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .location(event.getLocation())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .notificationTime(event.getNotificationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .notificationType(event.getNotificationType())
                .remindBeforeMinutes(event.getRemindBeforeMinutes())
                .repeatType(event.getRepeatType())
                .repeatUntil(event.getRepeatUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .color(event.getColor())
                .category(event.getCategory())
                .priority(event.getPriority())
                .pinned(event.getPinned())
                .groupId(event.getGroup().getGroupId())
                .imageUrl(event.getImageUrl())
                .createById(event.getCreateById())
                .assignees(event.getUsers().stream()
                        .map(u -> new EventUserResponse(u.getUserId(), u.getUsername(), u.getName()))
                        .toList())
                .build();
    }

    public static List<EventResponse> mapToProjection(List<EventProjection> projections) {
        Map<Long, EventResponse> grouped = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        for (EventProjection event : projections) {
            Long eventId = event.getEventId();

            // ถ้ายังไม่มี eventId นี้ -> สร้างใหม่
            EventResponse response = grouped.computeIfAbsent(eventId, id -> EventResponse.builder()
                    .eventId(event.getEventId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .startDate(formatSafe(event.getStartDate(), fmt))
                    .endDate(formatSafe(event.getEndDate(), fmt))
                    .location(event.getLocation())
                    .latitude(event.getLatitude())
                    .longitude(event.getLongitude())
                    .notificationTime(formatSafe(event.getNotificationTime(), fmt))
                    .notificationType(event.getNotificationType())
                    .remindBeforeMinutes(event.getRemindBeforeMinutes())
                    .repeatType(event.getRepeatType())
                    .repeatUntil(formatSafe(event.getRepeatUntil(), fmt))
                    .color(event.getColor())
                    .category(event.getCategory())
                    .priority(event.getPriority())
                    .pinned(event.getPinned())
                    .groupId(event.getGroupId())
                    .imageUrl(event.getImageUrl())
                    .assignees(new ArrayList<>()) // ✅ เตรียม list user
                    .build()
            );

            // ✅ เพิ่ม user เข้า list ถ้ามี
            if (event.getUserId() != null) {
                response.assignees().add(new EventUserResponse(
                        event.getUserId(),
                        event.getUsername(),
                        event.getName()
                ));
            }
        }

        // ✅ คืนค่าเป็น list ของ EventResponse
        return new ArrayList<>(grouped.values());
    }

    private static String formatSafe(LocalDateTime time, DateTimeFormatter fmt) {
        return (time != null) ? time.format(fmt) : null;
    }


}
