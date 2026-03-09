package course.service;

import course.dto.NewsRequest;
import course.dto.NewsResponse;
import course.dto.PageResponse;
import course.dto.UploadResponse;
import course.exception.BadRequestException;
import course.util.HtmlSanitizer;
import course.exception.ResourceNotFoundException;
import course.model.News;
import course.repository.NewsRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import course.model.Article;
import course.repository.ArticleRepository;
import java.util.ArrayList;
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
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", id));

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
    public void deleteNews(String id) {
        if (!newsRepository.existsById(id != null ? id : "")) {
            throw new ResourceNotFoundException("News", "id", id);
        }
        newsRepository.deleteById(id != null ? id : "");
    }

    public NewsResponse getNewsById(String id) {
        News news = newsRepository.findById(id != null ? id : "")
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", id));
        return mapToResponse(news);
    }

    public PageResponse<NewsResponse> getAllNews(Pageable pageable) {
        List<NewsResponse> newsList = newsRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> articleList = articleRepository.findAll().stream()
                .map(this::mapArticleToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> combined = Stream.concat(newsList.stream(), articleList.stream())
                .sorted(Comparator.comparing(NewsResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getPageResponse(combined, pageable);
    }

    public PageResponse<NewsResponse> getPublishedNews(Pageable pageable) {
        List<NewsResponse> newsList = newsRepository.findByIsPublished(true, Pageable.unpaged()).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> articleList = articleRepository.findByIsPublished(true, Pageable.unpaged()).getContent()
                .stream()
                .map(this::mapArticleToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> combined = Stream.concat(newsList.stream(), articleList.stream())
                .sorted(Comparator.comparing(NewsResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getPageResponse(combined, pageable);
    }

    public PageResponse<NewsResponse> searchNews(String keyword, Pageable pageable) {
        List<NewsResponse> newsList = newsRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, Pageable.unpaged()).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> articleList = articleRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, Pageable.unpaged()).getContent().stream()
                .map(this::mapArticleToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> combined = Stream.concat(newsList.stream(), articleList.stream())
                .sorted(Comparator.comparing(NewsResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());

        return getPageResponse(combined, pageable);
    }

    private PageResponse<NewsResponse> getPageResponse(List<NewsResponse> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        List<NewsResponse> content = new ArrayList<>();
        if (start < list.size()) {
            content = list.subList(start, end);
        }

        return PageResponse.<NewsResponse>builder()
                .content(content)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(list.size())
                .totalPages((int) Math.ceil((double) list.size() / pageable.getPageSize()))
                .last(end == list.size())
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
