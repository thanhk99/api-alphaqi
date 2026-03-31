package course.service;

import course.dto.NewsRequest;
import course.dto.NewsResponse;
import course.dto.PageResponse;
import course.dto.UploadResponse;
import course.exception.BadRequestException;
import course.util.HtmlSanitizer;
import course.exception.ResourceNotFoundException;
import course.model.News;
import course.dto.projection.NewsArticleProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import course.model.Article;
import course.model.enums.ArticleType;
import course.repository.ArticleRepository;
import course.repository.NewsRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final ArticleRepository articleRepository;
    private final CloudinaryService cloudinaryService;

    public NewsService(NewsRepository newsRepository, ArticleRepository articleRepository,
            CloudinaryService cloudinaryService) {
        this.newsRepository = newsRepository;
        this.articleRepository = articleRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public NewsResponse createNews(NewsRequest request, MultipartFile thumbnail) {
        News news = new News();
        updateNewsFromRequest(news, request);

        // Upload thumbnail if provided
        if (thumbnail != null && !thumbnail.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
            news.setThumbnail(uploadResponse.getUrl());
        } else if (request.getThumbnailUrl() != null) {
            news.setThumbnail(request.getThumbnailUrl());
        }

        news = newsRepository.save(news);
        return mapToResponse(news);
    }

    @Transactional
    public NewsResponse updateNews(String id, NewsRequest request, MultipartFile thumbnail) {
        if (id == null) {
            throw new BadRequestException("News ID must not be null");
        }

        // Try to find in News first
        var newsOptional = newsRepository.findById(id);
        if (newsOptional.isPresent()) {
            News news = newsOptional.get();
            updateNewsFromRequest(news, request);
            if (thumbnail != null && !thumbnail.isEmpty()) {
                UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
                news.setThumbnail(uploadResponse.getUrl());
            } else if (request.getThumbnailUrl() != null) {
                news.setThumbnail(request.getThumbnailUrl());
            }
            news = newsRepository.save(news);
            return mapToResponse(news);
        }

        // If not found in News, try Articles
        var articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            updateArticleFromRequest(article, request);
            if (thumbnail != null && !thumbnail.isEmpty()) {
                UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
                article.setThumbnail(uploadResponse.getUrl());
            } else if (request.getThumbnailUrl() != null) {
                article.setThumbnail(request.getThumbnailUrl());
            }
            article = articleRepository.save(article);
            return mapArticleToResponse(article);
        }

        throw new ResourceNotFoundException("News/Article", "id", id);
    }

    @Transactional
    public void deleteNews(String id) {
        String safeId = id != null ? id : "";
        if (newsRepository.existsById(safeId)) {
            newsRepository.deleteById(safeId);
        } else if (articleRepository.existsById(safeId)) {
            articleRepository.deleteById(safeId);
        } else {
            throw new ResourceNotFoundException("News/Article", "id", id);
        }
    }

    public NewsResponse getNewsById(String id) {
        String safeId = id != null ? id : "";
        return newsRepository.findById(safeId)
                .map(this::mapToResponse)
                .orElseGet(() -> articleRepository.findById(safeId)
                        .map(this::mapArticleToResponse)
                        .orElseThrow(() -> new ResourceNotFoundException("News/Article", "id", id)));
    }

    public PageResponse<NewsResponse> getAllNews(Pageable pageable) {
        Page<NewsArticleProjection> pageResult = newsRepository.findAllNewsAndArticles(pageable);
        return mapToPageResponse(pageResult);
    }

    public PageResponse<NewsResponse> getPublishedNews(Pageable pageable) {
        Page<NewsArticleProjection> pageResult = newsRepository.findPublishedNewsAndArticles(pageable);
        return mapToPageResponse(pageResult);
    }

    public PageResponse<NewsResponse> searchNews(String keyword, Pageable pageable) {
        Page<NewsArticleProjection> pageResult = newsRepository.searchNewsAndArticles(keyword, pageable);
        return mapToPageResponse(pageResult);
    }

    private PageResponse<NewsResponse> mapToPageResponse(Page<NewsArticleProjection> pageResult) {
        List<NewsResponse> content = pageResult.getContent().stream()
                .map(this::mapProjectionToResponse)
                .collect(Collectors.toList());

        return PageResponse.<NewsResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    private NewsResponse mapProjectionToResponse(NewsArticleProjection proj) {
        return NewsResponse.builder()
                .id(proj.getId())
                .title(proj.getTitle())
                .description(proj.getDescription())
                .content(null) // Content explicitly not fetched for listing performance
                .thumbnail(proj.getThumbnail())
                .type("NEWS".equals(proj.getType()) ? "NEWS" : "BLOG")
                .isPublished(proj.getIsPublished())
                .isShowHome(proj.getIsShowHome())
                .createdAt(proj.getCreatedAt())
                .updatedAt(proj.getUpdatedAt())
                .build();
    }

    public List<NewsResponse> getFeaturedNews() {
        List<NewsResponse> newsList = newsRepository.findTop8ByIsPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> articleList = articleRepository.findTop8ByIsPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::mapArticleToResponse)
                .collect(Collectors.toList());

        return Stream.concat(newsList.stream(), articleList.stream())
                .sorted(Comparator.comparing(NewsResponse::getCreatedAt).reversed())
                .limit(8)
                .collect(Collectors.toList());
    }

    private void updateNewsFromRequest(News news, NewsRequest request) {
        news.setTitle(request.getTitle());
        news.setDescription(HtmlSanitizer.sanitize(request.getDescription()));
        news.setContent(HtmlSanitizer.sanitize(request.getContent()));
        news.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : false);
        news.setIsShowHome(request.getIsShowHome() != null ? request.getIsShowHome() : false);
    }

    private void updateArticleFromRequest(Article article, NewsRequest request) {
        article.setTitle(request.getTitle());
        article.setDescription(HtmlSanitizer.sanitize(request.getDescription()));
        article.setContent(HtmlSanitizer.sanitize(request.getContent()));
        article.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : false);

        if (request.getLink() != null) {
            article.setLink(request.getLink());
        }

        if (request.getType() != null) {
            try {
                article.setType(ArticleType.valueOf(request.getType()));
            } catch (IllegalArgumentException e) {
                // Keep existing type if invalid
            }
        }
    }

    private NewsResponse mapToResponse(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .description(news.getDescription())
                .content(news.getContent())
                .thumbnail(news.getThumbnail())
                .type("NEWS")
                .isPublished(news.getIsPublished())
                .isShowHome(news.getIsShowHome())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }

    private NewsResponse mapArticleToResponse(Article article) {
        return NewsResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .content(article.getContent())
                .thumbnail(article.getThumbnail())
                .type("BLOG")
                .isPublished(article.getIsPublished())
                .isShowHome(false) // Articles don't have isShowHome property
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
