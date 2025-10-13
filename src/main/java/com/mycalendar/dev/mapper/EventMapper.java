package com.mycalendar.dev.mapper;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.payload.response.event.EventUserResponse;

import java.time.format.DateTimeFormatter;

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
}
