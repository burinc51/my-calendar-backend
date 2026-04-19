package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.ReportRequest;
import com.mycalendar.dev.payload.request.UpdateReportStatusRequest;
import com.mycalendar.dev.payload.request.filter.ReportFilterRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.PaginationWithFilterRequest;
import com.mycalendar.dev.payload.response.ReportResponse;

public interface IReportService {
    ReportResponse submitReport(ReportRequest request);
    PaginationResponse<ReportResponse> getAllReports(PaginationWithFilterRequest<ReportFilterRequest> request);
    ReportResponse getById(Long id);
    ReportResponse updateStatus(Long id, UpdateReportStatusRequest request);
}
