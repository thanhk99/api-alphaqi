package course.service;

import course.dto.FeaturedPostRequest;
import course.dto.FeaturedPostResponse;
import course.dto.PageResponse;
import course.dto.UploadResponse;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.FeaturedPost;
import course.repository.FeaturedPostRepository;
import course.util.HtmlFileExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeaturedPostService {

    private final FeaturedPostRepository featuredPostRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public FeaturedPostResponse createFeaturedPost(FeaturedPostRequest request, MultipartFile thumbnail, MultipartFile htmlFile) {
        FeaturedPost post = new FeaturedPost();
        updatePostFromRequest(post, request);

        // Upload thumbnail if provided
        if (thumbnail != null && !thumbnail.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
            post.setThumbnail(uploadResponse.getUrl());
        } else if (request.getThumbnailUrl() != null) {
            post.setThumbnail(request.getThumbnailUrl());
        }

        // Upload HTML file if provided
        if (htmlFile != null && !htmlFile.isEmpty()) {
            // Upload as raw to preserve HTML
            UploadResponse uploadResponse = cloudinaryService.upload(htmlFile, "featured-html");
            post.setHtmlUrl(uploadResponse.getUrl());
        }

        post = featuredPostRepository.save(post);
        return mapToResponse(post);
    }

    @Transactional
    public FeaturedPostResponse updateFeaturedPost(String id, FeaturedPostRequest request, MultipartFile thumbnail, MultipartFile htmlFile) {
        FeaturedPost post = featuredPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeaturedPost", "id", id));

        updatePostFromRequest(post, request);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
            post.setThumbnail(uploadResponse.getUrl());
        } else if (request.getThumbnailUrl() != null) {
            post.setThumbnail(request.getThumbnailUrl());
        }

        if (htmlFile != null && !htmlFile.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.upload(htmlFile, "featured-html");
            post.setHtmlUrl(uploadResponse.getUrl());
        }

        post = featuredPostRepository.save(post);
        return mapToResponse(post);
    }

    @Transactional
    public void deleteFeaturedPost(String id) {
        if (!featuredPostRepository.existsById(id)) {
            throw new ResourceNotFoundException("FeaturedPost", "id", id);
        }
        featuredPostRepository.deleteById(id);
    }

    public FeaturedPostResponse getFeaturedPostById(String id) {
        return featuredPostRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("FeaturedPost", "id", id));
    }

    public PageResponse<FeaturedPostResponse> getAllFeaturedPosts(Pageable pageable) {
        Page<FeaturedPost> pageResult = featuredPostRepository.findAll(pageable);
        return mapToPageResponse(pageResult);
    }

    public List<FeaturedPostResponse> getActiveFeaturedPosts() {
        return featuredPostRepository.findTop10ByIsPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<FeaturedPostResponse> getPublishedFeaturedPosts(Pageable pageable) {
        Page<FeaturedPost> pageResult = featuredPostRepository.findByIsPublishedTrueOrderByCreatedAtDesc(pageable);
        return mapToPageResponse(pageResult);
    }

    /**
     * Fetch nội dung HTML từ Cloudinary URL, trả về string.
     * Controller dùng để serve đúng Content-Type: text/html.
     */
    public String fetchHtmlContent(String id) {
        FeaturedPost post = featuredPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeaturedPost", "id", id));

        String htmlUrl = post.getHtmlUrl();
        log.info("[FeaturedPost] Fetch HTML content, id={}, htmlUrl={}", id, htmlUrl);

        if (htmlUrl == null || htmlUrl.isBlank()) {
            throw new BadRequestException("Bài viết này không có file HTML đính kèm");
        }

        String rawHtml = fetchFromUrl(htmlUrl);
        // Trích xuất thành dạng embeddable (bỏ html/head/body)
        return HtmlFileExtractor.extractEmbeddableFromString(rawHtml);
    }

    /**
     * Fetch nội dung từ URL bên ngoài (Cloudinary).
     */
    @SuppressWarnings("deprecation")
    private String fetchFromUrl(String targetUrl) {
        try {
            // Dùng new URL() trực tiếp - tương thích tốt hơn với URL Cloudinary
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true); // Tự theo redirect
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000); // 10s
            conn.setReadTimeout(15000);    // 15s
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,*/*");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36");

            int status = conn.getResponseCode();
            log.info("[FeaturedPost] HTTP {} khi fetch: {}", status, targetUrl);

            if (status != HttpURLConnection.HTTP_OK) {
                log.error("[FeaturedPost] Lỗi HTTP {} khi fetch URL: {}", status, targetUrl);
                throw new BadRequestException("Không thể tải file từ Cloudinary (HTTP " + status + ")");
            }

            // Đọc encoding từ header, fallback UTF-8
            String contentType = conn.getContentType();
            String charset = "UTF-8";
            if (contentType != null && contentType.contains("charset=")) {
                charset = contentType.substring(contentType.indexOf("charset=") + 8).trim();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), charset))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String content = sb.toString();
                log.info("[FeaturedPost] Fetch thành công, {} bytes", content.length());
                return content;
            }
        } catch (BadRequestException e) {
            throw e; // Re-throw, không bọc lại
        } catch (Exception e) {
            log.error("[FeaturedPost] Lỗi fetch HTML từ: {}, error: {}", targetUrl, e.getMessage(), e);
            throw new BadRequestException("Lỗi khi tải nội dung bài viết: " + e.getMessage());
        }
    }

    private void updatePostFromRequest(FeaturedPost post, FeaturedPostRequest request) {
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPriority(request.getPriority());
        post.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : false);
    }

    private FeaturedPostResponse mapToResponse(FeaturedPost post) {
        return FeaturedPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .thumbnail(post.getThumbnail())
                .htmlUrl(post.getHtmlUrl())
                .priority(post.getPriority())
                .isPublished(post.getIsPublished())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private PageResponse<FeaturedPostResponse> mapToPageResponse(Page<FeaturedPost> pageResult) {
        List<FeaturedPostResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<FeaturedPostResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }
}
