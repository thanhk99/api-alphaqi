package course.controller;

import course.dto.AdminEnrollmentResponse;
import course.dto.EnrollmentCheckResponse;
import course.dto.EnrollmentRequest;
import course.dto.EnrollmentResponse;
import course.dto.EnrollmentStatisticsResponse;
import course.dto.PageResponse;
import course.dto.UpdateEnrollmentStatusRequest;
import course.model.Enrollment;
import course.model.enums.EnrollmentStatus;
import course.service.EnrollmentService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import course.security.CustomUserDetails;

@RestController
@RequestMapping("/enrollments")
@PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<Enrollment>> enrollCourse(
            @Valid @RequestBody EnrollmentRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();
        Enrollment enrollment = enrollmentService.enrollCourse(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enrolled successfully", enrollment));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getMyEnrollments(
            Authentication authentication,
            Pageable pageable) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();
        PageResponse<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/check/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ApiResponse<EnrollmentCheckResponse>> checkEnrollment(
            @PathVariable String courseId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();
        boolean enrolled = enrollmentService.checkEnrollment(userId, courseId);
        EnrollmentCheckResponse response = EnrollmentCheckResponse.builder()
                .enrolled(enrolled)
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<Enrollment>> updateProgress(
            @PathVariable String id,
            @RequestParam Double progress) {
        Enrollment enrollment = enrollmentService.updateProgress(id, progress);
        return ResponseEntity.ok(ApiResponse.success("Progress updated successfully", enrollment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelEnrollment(@PathVariable String id) {
        enrollmentService.cancelEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment cancelled successfully", null));
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminEnrollmentResponse>>> getAllEnrollments(Pageable pageable) {
        PageResponse<AdminEnrollmentResponse> enrollments = enrollmentService.getAllEnrollmentsForAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/admin/course/{courseId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminEnrollmentResponse>>> getEnrollmentsByCourse(
            @PathVariable String courseId,
            Pageable pageable) {
        PageResponse<AdminEnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByCourseForAdmin(courseId,
                pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminEnrollmentResponse>>> getEnrollmentsByStatus(
            @PathVariable EnrollmentStatus status,
            Pageable pageable) {
        PageResponse<AdminEnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByStatusForAdmin(status,
                pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminEnrollmentResponse>>> searchEnrollments(
            @RequestParam String keyword,
            Pageable pageable) {
        PageResponse<AdminEnrollmentResponse> enrollments = enrollmentService.searchEnrollmentsForAdmin(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<EnrollmentStatisticsResponse>> getEnrollmentStatistics() {
        EnrollmentStatisticsResponse statistics = enrollmentService.getEnrollmentStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Enrollment>> updateEnrollmentStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request) {
        Enrollment enrollment = enrollmentService.updateEnrollmentStatusByAdmin(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Enrollment status updated successfully", enrollment));
    }
}
