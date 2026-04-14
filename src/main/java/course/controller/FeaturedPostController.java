package course.controller;

import course.dto.FeaturedPostResponse;
import course.dto.PageResponse;
import course.service.FeaturedPostService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import course.dto.FeaturedPostRequest;

import java.util.List;

@RestController
@RequestMapping("/featured-posts")
@RequiredArgsConstructor
public class FeaturedPostController {

    private final FeaturedPostService featuredPostService;

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<FeaturedPostResponse>> createFeaturedPost(
            @ModelAttribute @Valid FeaturedPostRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "htmlFile", required = false) MultipartFile htmlFile) {
        FeaturedPostResponse response = featuredPostService.createFeaturedPost(request, thumbnail, htmlFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Featured post created successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<FeaturedPostResponse>> updateFeaturedPost(
            @PathVariable String id,
            @ModelAttribute @Valid FeaturedPostRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "htmlFile", required = false) MultipartFile htmlFile) {
        FeaturedPostResponse response = featuredPostService.updateFeaturedPost(id, request, thumbnail, htmlFile);
        return ResponseEntity.ok(ApiResponse.success("Featured post updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteFeaturedPost(@PathVariable String id) {
        featuredPostService.deleteFeaturedPost(id);
        return ResponseEntity.ok(ApiResponse.success("Featured post deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeaturedPostResponse>> getFeaturedPostById(@PathVariable String id) {
        FeaturedPostResponse response = featuredPostService.getFeaturedPostById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeaturedPostResponse>>> getAllFeaturedPosts(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<FeaturedPostResponse> posts = featuredPostService.getAllFeaturedPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<FeaturedPostResponse>>> getActiveFeaturedPosts() {
        List<FeaturedPostResponse> posts = featuredPostService.getActiveFeaturedPosts();
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * Proxy endpoint: fetch HTML từ Cloudinary và trả về đúng Content-Type: text/html.
     * Frontend dùng <iframe src="/api/featured-posts/{id}/content"> để render đúng.
     * HTML không lưu DB — vẫn giữ trên Cloudinary.
     */
    @GetMapping(value = "/{id}/content", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getFeaturedPostHtmlContent(@PathVariable String id) {
        String htmlContent = featuredPostService.fetchHtmlContent(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlContent);
    }
}
