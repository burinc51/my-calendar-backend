package com.mycalendar.dev.payload.request.filter;

import com.mycalendar.dev.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReportFilterRequest {
    @Schema(example = "PENDING")
    private ReportStatus status;
    @Schema(example = "BUG")
    private String category;
    @Schema(example = "john")
    private String username;
}
