package com.mycalendar.dev.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventResponse {
    private Long eventId;
    private Long userId;
    private String title;
    private String description;
    private String imageUrl;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    private String location;
    private boolean isPinned;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime notificationTime;
    private String repeating;
    private String color;
    private String category;
    private String priority;
    private String groupId;
    private List<AssigneeDTO> assignees;

    @Getter
    @Setter
    public static class AssigneeDTO {
        private Long userId;
        private String userName;
    }
}
