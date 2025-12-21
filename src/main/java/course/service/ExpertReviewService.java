package course.service;

import course.dto.ExpertReviewRequest;
import course.dto.ExpertReviewResponse;
import course.dto.UploadResponse;
import course.exception.ResourceNotFoundException;
import course.model.Course;
import course.model.ExpertReview;
import course.repository.CourseRepository;
import course.repository.ExpertReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExpertReviewService {

    private final ExpertReviewRepository expertReviewRepository;
    private final CourseRepository courseRepository;
    private final CloudinaryService cloudinaryService;

    public ExpertReviewService(ExpertReviewRepository expertReviewRepository,
            CourseRepository courseRepository,
            CloudinaryService cloudinaryService) {
        this.expertReviewRepository = expertReviewRepository;
        this.courseRepository = courseRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public ExpertReviewResponse createExpertReview(ExpertReviewRequest request, MultipartFile avatar) {
        ExpertReview review = new ExpertReview();
        review.setExpertName(request.getExpertName());
        review.setExpertTitle(request.getExpertTitle());
        review.setContent(request.getContent());
        review.setRating(request.getRating());

        if (avatar != null && !avatar.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(avatar);
            review.setExpertAvatar(uploadResponse.getUrl());
        } else {
            review.setExpertAvatar(request.getExpertAvatarUrl());
        }

        if (request.getCourseId() != null && !request.getCourseId().isEmpty()) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            review.setCourse(course);
        }

        review = expertReviewRepository.save(review);
        return mapToResponse(review);
    }

    public List<ExpertReviewResponse> getAllExpertReviews() {
        return expertReviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExpertReviewResponse getExpertReviewById(String id) {
        ExpertReview review = expertReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expert Review not found"));
        return mapToResponse(review);
    }

    @Transactional
    public ExpertReviewResponse updateExpertReview(String id, ExpertReviewRequest request, MultipartFile avatar) {
        ExpertReview review = expertReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expert Review not found"));

        review.setExpertName(request.getExpertName());
        review.setExpertTitle(request.getExpertTitle());
        review.setContent(request.getContent());
        review.setRating(request.getRating());

        if (avatar != null && !avatar.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(avatar);
            review.setExpertAvatar(uploadResponse.getUrl());
        } else if (request.getExpertAvatarUrl() != null) {
            review.setExpertAvatar(request.getExpertAvatarUrl());
        }

        if (request.getCourseId() != null) {
            if (request.getCourseId().isEmpty()) {
                review.setCourse(null);
            } else {
                Course course = courseRepository.findById(request.getCourseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
                review.setCourse(course);
            }
        }

        review = expertReviewRepository.save(review);
        return mapToResponse(review);
    }

    @Transactional
    public void deleteExpertReview(String id) {
        if (!expertReviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expert Review not found");
        }
        expertReviewRepository.deleteById(id);
    }

    private ExpertReviewResponse mapToResponse(ExpertReview review) {
        return ExpertReviewResponse.builder()
                .id(review.getId())
                .expertName(review.getExpertName())
                .expertTitle(review.getExpertTitle())
                .expertAvatar(review.getExpertAvatar())
                .content(review.getContent())
                .rating(review.getRating())
                .courseId(review.getCourse() != null ? review.getCourse().getId() : null)
                .courseName(review.getCourse() != null ? review.getCourse().getTitle() : null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
