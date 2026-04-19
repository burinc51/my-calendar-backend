package com.mycalendar.dev.payload.request;

import com.mycalendar.dev.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReportStatusRequest {

    @NotNull(message = "status is required")
    private ReportStatus status;
}
