package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.response.event.EventResponse;
import com.mycalendar.dev.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Schedule")
public class NotificationScheduleController {

    private final INotificationService notificationService;

    /**
     * Get events scheduled to notify on a date.
     * If date is omitted, uses today in Asia/Bangkok.
     */
    @GetMapping("/schedule")
    @Operation(
            summary = "Get notification schedule by date",
            description = "Returns events whose notificationTime falls on the selected date. " +
                    "By default, only pending notifications are returned (notificationSent=false)."
    )
    public ResponseEntity<List<EventResponse>> getNotificationSchedule(
            @Parameter(description = "Date in yyyy-MM-dd. If omitted, today is used.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @Parameter(description = "true = include already sent notifications")
            @RequestParam(defaultValue = "false") boolean includeSent,
            @Parameter(description = "Optional filter by group id")
            @RequestParam(required = false) Long groupId
    ) {
        return ResponseEntity.ok(notificationService.getNotificationScheduleByDate(date, includeSent, groupId));
    }
}

