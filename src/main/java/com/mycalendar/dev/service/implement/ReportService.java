package com.mycalendar.dev.service.implement;

import com.mycalendar.dev.entity.Report;
import com.mycalendar.dev.enums.ReportStatus;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.request.ReportRequest;
import com.mycalendar.dev.payload.request.UpdateReportStatusRequest;
import com.mycalendar.dev.payload.request.filter.ReportFilterRequest;
import com.mycalendar.dev.payload.response.PaginationResponse;
import com.mycalendar.dev.payload.response.PaginationWithFilterRequest;
import com.mycalendar.dev.payload.response.ReportResponse;
import com.mycalendar.dev.repository.ReportRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.service.IReportService;
import com.mycalendar.dev.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.mycalendar.dev.util.TypeSafe.validateSortBy;

@Service
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    // ─── User ──────────────────────────────────────────────────────────────────

    @Override
    public ReportResponse submitReport(ReportRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();

        Report report = new Report();
        report.setUserId(userId);
        report.setCategory(request.getCategory());
        report.setDetail(request.getDetail());
        report.setStatus(ReportStatus.PENDING);

        Report saved = reportRepository.save(report);
        return toResponse(saved, resolveUsername(userId));
    }

    // ─── Admin ─────────────────────────────────────────────────────────────────

    @Override
    public PaginationResponse<ReportResponse> getAllReports(PaginationWithFilterRequest<ReportFilterRequest> request) {
        // Validate sortBy against ReportResponse fields (id, userId, category, status, createdAt, updatedAt)
        validateSortBy(request.getSortBy(), ReportResponse.class);

        PageRequest pageable = request.getPageRequest();
        ReportFilterRequest filter = request.getFilter();

        ReportStatus status   = (filter != null) ? filter.getStatus()   : null;
        String category       = (filter != null) ? filter.getCategory() : null;
        String username       = (filter != null) ? filter.getUsername() : null;

        Page<Report> page = reportRepository.findAllWithFilters(status, category, username, pageable);
        Page<ReportResponse> mapped = page.map(r -> toResponse(r, resolveUsername(r.getUserId())));
        return new PaginationResponse<>(mapped);
    }

    @Override
    public ReportResponse getById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("report", "id", String.valueOf(id)));
        return toResponse(report, resolveUsername(report.getUserId()));
    }

    @Override
    public ReportResponse updateStatus(Long id, UpdateReportStatusRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("report", "id", String.valueOf(id)));
        report.setStatus(request.getStatus());
        Report saved = reportRepository.save(report);
        return toResponse(saved, resolveUsername(saved.getUserId()));
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private String resolveUsername(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(u -> u.getUsername())
                .orElse(null);
    }

    private ReportResponse toResponse(Report r, String username) {
        return ReportResponse.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .username(username)
                .category(r.getCategory())
                .detail(r.getDetail())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
