package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.ReportRequest;
import com.mycalendar.dev.payload.request.UpdateReportStatusRequest;
import com.mycalendar.dev.payload.request.filter.ReportFilterRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.PaginationWithFilterRequest;
import com.mycalendar.dev.payload.response.ReportResponse;
import com.mycalendar.dev.service.IReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportRestController {

    private final IReportService reportService;

    public ReportRestController(IReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * POST /api/v1/reports
     * User ส่งรายงานปัญหา (ต้อง login)
     */
    @PostMapping
    public ResponseEntity<ReportResponse> submitReport(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.submitReport(request));
    }

    /**
     * POST /api/v1/reports/all
     * Admin ดูรายการรายงานทั้งหมด พร้อม pagination, sort และ filter
     */
    @PostMapping("/all")
    public PaginationResponse<ReportResponse> getAllReports(
            @RequestBody PaginationWithFilterRequest<ReportFilterRequest> request) {
        return reportService.getAllReports(request);
    }

    /**
     * GET /api/v1/reports/{id}
     * ดูรายละเอียดรายงานตาม id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    /**
     * PATCH /api/v1/reports/{id}/status
     * Admin อัปเดตสถานะรายงาน
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusRequest request) {
        return ResponseEntity.ok(reportService.updateStatus(id, request));
    }
}
