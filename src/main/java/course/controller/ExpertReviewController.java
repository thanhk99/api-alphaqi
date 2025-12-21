package course.controller;

import course.dto.ExpertReviewRequest;
import course.dto.ExpertReviewResponse;
import course.service.ExpertReviewService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/expert-reviews")
public class ExpertReviewController {

    private final ExpertReviewService expertReviewService;

    public ExpertReviewController(ExpertReviewService expertReviewService) {
        this.expertReviewService = expertReviewService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ExpertReviewResponse>> createExpertReview(
            @ModelAttribute @Valid ExpertReviewRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        ExpertReviewResponse response = expertReviewService.createExpertReview(request, avatar);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expert Review created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpertReviewResponse>>> getAllExpertReviews() {
        List<ExpertReviewResponse> responses = expertReviewService.getAllExpertReviews();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpertReviewResponse>> getExpertReviewById(@PathVariable String id) {
        ExpertReviewResponse response = expertReviewService.getExpertReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ExpertReviewResponse>> updateExpertReview(
            @PathVariable String id,
            @ModelAttribute @Valid ExpertReviewRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        ExpertReviewResponse response = expertReviewService.updateExpertReview(id, request, avatar);
        return ResponseEntity.ok(ApiResponse.success("Expert Review updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExpertReview(@PathVariable String id) {
        expertReviewService.deleteExpertReview(id);
        return ResponseEntity.ok(ApiResponse.success("Expert Review deleted successfully", null));
    }
}
