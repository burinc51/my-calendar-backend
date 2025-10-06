package com.mycalendar.dev.payload.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class EventResponse {
    private Long eventId;
    private Long userId;
    private String title;
    private String description;
    private String imageUrl;
    private String startDate;
    private String endDate;
    private String location;
    private boolean isPinned;
    private String notificationTime;
    private String repeating;
    private String color;
    private String category;
    private String priority;
    private Long groupId;
    private List<AssigneeDTO> assignees;

    @Getter
    @Setter
    public static class AssigneeDTO {
        private Long userId;
        private String userName;
    }
}
