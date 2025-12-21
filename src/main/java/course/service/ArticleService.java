package course.service;

import course.dto.ArticleRequest;
import course.dto.ArticleResponse;
import course.dto.UploadResponse;
import course.util.HtmlSanitizer;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.Article;
import course.model.enums.ArticleType;
import course.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CloudinaryService cloudinaryService;

    public ArticleService(ArticleRepository articleRepository, CloudinaryService cloudinaryService) {
        this.articleRepository = articleRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public ArticleResponse createArticle(ArticleRequest request, MultipartFile thumbnail) {
        Article article = new Article();
        updateArticleFromRequest(article, request);

        // Upload thumbnail if provided
        if (thumbnail != null && !thumbnail.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
            article.setThumbnail(uploadResponse.getUrl());
        } else {
            article.setThumbnail(request.getThumbnailUrl());
        }

        article = articleRepository.save(article);
        return mapToResponse(article);
    }

    @Transactional
    public ArticleResponse updateArticle(String id, ArticleRequest request, MultipartFile thumbnail) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        updateArticleFromRequest(article, request);

        // Upload thumbnail if provided
        if (thumbnail != null && !thumbnail.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
            article.setThumbnail(uploadResponse.getUrl());
        } else if (request.getThumbnailUrl() != null) {
            article.setThumbnail(request.getThumbnailUrl());
        }

        article = articleRepository.save(article);
        return mapToResponse(article);
    }

    @Transactional
    public void deleteArticle(String id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article", "id", id);
        }
        articleRepository.deleteById(id);
    }

    public ArticleResponse getArticleById(String id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        return mapToResponse(article);
    }

    public List<ArticleResponse> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ArticleResponse> getPublishedArticles() {
        return articleRepository.findByIsPublished(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void updateArticleFromRequest(Article article, ArticleRequest request) {
        article.setTitle(request.getTitle());
        article.setDescription(HtmlSanitizer.sanitize(request.getDescription()));

        try {
            ArticleType type = ArticleType.valueOf(request.getType().toUpperCase());
            article.setType(type);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid article type: " + request.getType());
        }

        if (article.getType() == ArticleType.INTERNAL) {
            if (request.getContent() == null || request.getContent().isEmpty()) {
                throw new BadRequestException("Content is required for INTERNAL articles");
            }
            article.setContent(HtmlSanitizer.sanitize(request.getContent()));
            article.setLink(null);
        } else {
            if (request.getLink() == null || request.getLink().isEmpty()) {
                throw new BadRequestException("Link is required for EXTERNAL articles");
            }
            article.setLink(request.getLink());
            article.setContent(null);
        }

        article.setIsPublished(request.getIsPublished());
    }

    private ArticleResponse mapToResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .content(article.getContent())
                .link(article.getLink())
                .type(article.getType().name())
                .thumbnail(article.getThumbnail())
                .isPublished(article.getIsPublished())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
