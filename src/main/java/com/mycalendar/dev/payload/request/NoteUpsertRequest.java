package com.mycalendar.dev.payload.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class NoteUpsertRequest {

    private String title;
    private String content;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be valid hex format (e.g. #ffecb3)")
    private String color;

    private Boolean isPinned;

    private List<String> tags;

    private OffsetDateTime reminderDate;

    @Pattern(regexp = "^(none|daily|weekly|monthly|yearly)$", message = "Recurrence must be one of: none, daily, weekly, monthly, yearly")
    private String recurrence;

    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    private String locationName;

    @Pattern(regexp = "^(https?://).+", message = "Location link must be a valid URL")
    private String locationLink;

    public boolean hasAnyContent() {
        return hasText(title) || hasText(content);
    }

    public boolean hasValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

