package course.service;

import course.dto.FeaturedPostRequest;
import course.dto.FeaturedPostResponse;
import course.dto.PageResponse;
import course.dto.UploadResponse;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.FeaturedPost;
import course.repository.FeaturedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
