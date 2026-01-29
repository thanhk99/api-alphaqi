package course.service;

import course.dto.NewsRequest;
import course.dto.NewsResponse;
import course.dto.UploadResponse;
import course.util.HtmlSanitizer;
import course.exception.ResourceNotFoundException;
import course.model.News;
import course.repository.NewsRepository;
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
        if (!newsRepository.existsById(id)) {
            throw new ResourceNotFoundException("News", "id", id);
        }
        newsRepository.deleteById(id);
    }

    public NewsResponse getNewsById(String id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", id));
        return mapToResponse(news);
    }

    public List<NewsResponse> getAllNews() {
        List<NewsResponse> newsList = newsRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> articleList = articleRepository.findAll().stream()
                .map(this::mapArticleToResponse)
                .collect(Collectors.toList());

        return Stream.concat(newsList.stream(), articleList.stream())
                .sorted(Comparator.comparing(NewsResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<NewsResponse> getPublishedNews() {
        List<NewsResponse> newsList = newsRepository.findByIsPublished(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<NewsResponse> articleList = articleRepository.findByIsPublished(true).stream()
                .map(this::mapArticleToResponse)
                .collect(Collectors.toList());

        return Stream.concat(newsList.stream(), articleList.stream())
                .sorted(Comparator.comparing(NewsResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());
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
