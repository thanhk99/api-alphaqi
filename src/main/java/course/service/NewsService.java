package course.service;

import course.dto.NewsRequest;
import course.dto.NewsResponse;
import course.dto.UploadResponse;
import course.util.HtmlSanitizer;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.News;
import course.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final CloudinaryService cloudinaryService;

    public NewsService(NewsRepository newsRepository, CloudinaryService cloudinaryService) {
        this.newsRepository = newsRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public NewsResponse createNews(NewsRequest request, MultipartFile thumbnail) {
        // Validate isShowHome limit (max 8)
        if (Boolean.TRUE.equals(request.getIsShowHome())) {
            validateIsShowHomeLimit();
        }

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

        // Validate isShowHome limit if changing to true
        if (Boolean.TRUE.equals(request.getIsShowHome()) && !Boolean.TRUE.equals(news.getIsShowHome())) {
            validateIsShowHomeLimit();
        }

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
        return newsRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NewsResponse> getPublishedNews() {
        return newsRepository.findByIsPublished(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NewsResponse> getFeaturedNews() {
        return newsRepository.findTop8ByIsShowHomeTrueOrderByUpdatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateIsShowHomeLimit() {
        long count = newsRepository.countByIsShowHomeTrue();
        if (count >= 8) {
            throw new BadRequestException("Cannot set more than 8 news items to show on home page");
        }
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
                .isPublished(news.getIsPublished())
                .isShowHome(news.getIsShowHome())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }
}
