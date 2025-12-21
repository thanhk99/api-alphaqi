package course.controller;

import course.dto.NewsRequest;
import course.dto.NewsResponse;
import course.service.NewsService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<NewsResponse>> createNews(
            @ModelAttribute @Valid NewsRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        NewsResponse response = newsService.createNews(request, thumbnail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("News created successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<NewsResponse>> updateNews(
            @PathVariable String id,
            @ModelAttribute @Valid NewsRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        NewsResponse response = newsService.updateNews(id, request, thumbnail);
        return ResponseEntity.ok(ApiResponse.success("News updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteNews(@PathVariable String id) {
        newsService.deleteNews(id);
        return ResponseEntity.ok(ApiResponse.success("News deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsResponse>> getNewsById(@PathVariable String id) {
        NewsResponse response = newsService.getNewsById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getAllNews(
            @RequestParam(required = false) Boolean published) {
        List<NewsResponse> newsList = published != null && published
                ? newsService.getPublishedNews()
                : newsService.getAllNews();
        return ResponseEntity.ok(ApiResponse.success(newsList));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getFeaturedNews() {
        List<NewsResponse> newsList = newsService.getFeaturedNews();
        return ResponseEntity.ok(ApiResponse.success(newsList));
    }
}
