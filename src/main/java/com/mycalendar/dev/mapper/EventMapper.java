package com.mycalendar.dev.mapper;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.payload.response.event.EventUserResponse;
import com.mycalendar.dev.projection.EventProjection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.mycalendar.dev.util.RowMapperUtil.getValue;

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


    public static List<EventResponse> mapRowsMerged(List<Object[]> rows) {
        Map<Long, EventResponse> map = new LinkedHashMap<>();

        for (Object[] r : rows) {
            Long eventId = getValue(r, 0, Long.class);
            EventResponse e = map.computeIfAbsent(eventId, id ->
                    EventResponse.builder()
                            .eventId(getValue(r, 0, Long.class))
                            .title(getValue(r, 1, String.class))
                            .description(getValue(r, 2, String.class))
                            .startDate(Optional.ofNullable(getValue(r, 3, LocalDateTime.class))
                                    .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                    .orElse(null))
                            .endDate(Optional.ofNullable(getValue(r, 4, LocalDateTime.class))
                                    .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                    .orElse(null))
                            .location(getValue(r, 5, String.class))
                            .latitude(getValue(r, 6, Double.class))
                            .longitude(getValue(r, 7, Double.class))
                            .notificationTime(Optional.ofNullable(getValue(r, 8, LocalDateTime.class))
                                    .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                    .orElse(null))
                            .notificationType(getValue(r, 9, String.class))
                            .remindBeforeMinutes(getValue(r, 10, Integer.class))
                            .repeatType(getValue(r, 11, String.class))
                            .repeatUntil(Optional.ofNullable(getValue(r, 12, LocalDateTime.class))
                                    .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                    .orElse(null))
                            .color(getValue(r, 13, String.class))
                            .category(getValue(r, 14, String.class))
                            .priority(getValue(r, 15, String.class))
                            .pinned(getValue(r, 16, Boolean.class))
                            .imageUrl(getValue(r, 17, String.class))
                            .groupId(getValue(r, 18, Long.class))
                            .assignees(new ArrayList<>())
                            .build()
            );

            if (r[19] != null) {
                if (e.assignees().stream().noneMatch(a -> a.userId().equals(((Number) r[19]).longValue()))) {
                    e.assignees().add(new EventUserResponse(
                            ((Number) r[19]).longValue(),
                            (String) r[20],
                            (String) r[21]
                    ));
                }
            }
        }
        return new ArrayList<>(map.values());
    }


}
