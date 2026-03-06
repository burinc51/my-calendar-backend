package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class EventRequest {
    private Long eventId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String location;
    private Double latitude;
    private Double longitude;

    // ===== Notification =====
    /** notificationTime จะถูกคำนวณอัตโนมัติจาก remindBeforeMinutes ถ้าไม่ได้ระบุมาเอง */
    private LocalDateTime notificationTime;
    private String notificationType; // PUSH, EMAIL, POPUP
    /** จำนวนนาทีก่อนกิจกรรมเริ่มที่ต้องการแจ้งเตือน */
    private Integer remindBeforeMinutes;

    // ===== Repeat =====
    /** ประเภทการทำซ้ำ: NONE, DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM */
    private String repeatType;
    /** วันที่สิ้นสุดการทำซ้ำ */
    private LocalDateTime repeatUntil;
    /** ทำซ้ำทุกกี่หน่วย เช่น 1 = ทุกสัปดาห์, 2 = ทุก 2 สัปดาห์ */
    private Integer repeatInterval;
    /** วันที่ทำซ้ำสำหรับ WEEKLY/CUSTOM เช่น "MON,WED,FRI" */
    private String repeatDays;

    private String color;
    private String category;
    private String priority;
    private Boolean pinned;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Creator user ID is required")
    private Long createById;

    private Boolean allDay;
    private Set<Long> assigneeIds;
}


