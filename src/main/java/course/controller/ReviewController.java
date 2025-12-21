package course.controller;

import course.dto.ReviewRequest;
import course.dto.ReviewResponse;
import course.service.ReviewService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable String courseId,
            @Valid @RequestBody ReviewRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof course.security.CustomUserDetails) {
            course.security.CustomUserDetails userDetails = (course.security.CustomUserDetails) authentication
                    .getPrincipal();
            userId = userDetails.getId();
        } else if (authentication != null) {
            // Fallback or throw exception
            userId = authentication.getName();
        } else {
            throw new course.exception.BadRequestException("User not authenticated");
        }

        ReviewResponse response = reviewService.createReview(userId, courseId, request);
        return ResponseEntity.ok(ApiResponse.success("Review created successfully", response));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getCourseReviews(@PathVariable String courseId) {
        List<ReviewResponse> response = reviewService.getReviewsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof course.security.CustomUserDetails) {
            course.security.CustomUserDetails userDetails = (course.security.CustomUserDetails) authentication
                    .getPrincipal();
            userId = userDetails.getId();
        } else if (authentication != null) {
            userId = authentication.getName();
        } else {
            throw new course.exception.BadRequestException("User not authenticated");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        reviewService.deleteReview(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAllReviews() {
        List<ReviewResponse> response = reviewService.getAllReviews();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
