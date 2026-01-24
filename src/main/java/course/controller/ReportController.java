package course.controller;

import course.dto.ReportPagedResponse;
import course.dto.ReportRequest;
import course.dto.ReportResponse;
import course.model.enums.ReportType;
import course.service.ReportService;
import course.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getReportTypes() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportTypes()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReportPagedResponse>> getAllReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        ReportPagedResponse reports = reportService.getAllReports(type, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(@PathVariable Long id) {
        ReportResponse report = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam ReportType type,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        ReportRequest request = ReportRequest.builder()
                .title(title)
                .description(description)
                .type(type)
                .build();
        ReportResponse response = reportService.createReport(request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report created successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReport(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam ReportType type,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        ReportRequest request = ReportRequest.builder()
                .title(title)
                .description(description)
                .type(type)
                .build();
        ReportResponse response = reportService.updateReport(id, request, file);
        return ResponseEntity.ok(ApiResponse.success("Report updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report deleted successfully", null));
    }
}
