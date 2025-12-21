package course.controller;

import course.dto.ArticleRequest;
import course.dto.ArticleResponse;
import course.service.ArticleService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(
            @ModelAttribute @Valid ArticleRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        ArticleResponse response = articleService.createArticle(request, thumbnail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Article created successfully", response));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable String id,
            @ModelAttribute @Valid ArticleRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        ArticleResponse response = articleService.updateArticle(id, request, thumbnail);
        return ResponseEntity.ok(ApiResponse.success("Article updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteArticle(@PathVariable String id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleById(@PathVariable String id) {
        ArticleResponse response = articleService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getAllArticles(
            @RequestParam(required = false) Boolean published) {
        List<ArticleResponse> articles;
        if (published != null && published) {
            articles = articleService.getPublishedArticles();
        } else {
            articles = articleService.getAllArticles();
        }
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
}
