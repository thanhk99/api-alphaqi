package course.service;

import course.dto.ReviewRequest;
import course.dto.ReviewResponse;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.exception.UnauthorizedException;
import course.model.Course;
import course.model.Review;
import course.model.User;
import course.repository.CourseRepository;
import course.repository.ReviewRepository;
import course.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewResponse createReview(String userId, String courseId, ReviewRequest request) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Check if user already reviewed this course
        if (reviewRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new BadRequestException("You have already reviewed this course");
        }

        // Create review
        Review review = new Review();
        review.setUserId(userId);
        review.setCourseId(courseId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        // Update course statistics
        updateCourseRating(course);

        return mapToResponse(review, user.getFullName(), course.getTitle());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(review -> {
                    String userName = review.getUser() != null ? review.getUser().getFullName() : "Unknown User";
                    String courseTitle = review.getCourse() != null ? review.getCourse().getTitle() : "Unknown Course";
                    return mapToResponse(review, userName, courseTitle);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByCourse(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }

        return reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(review -> {
                    String userName = review.getUser() != null ? review.getUser().getFullName() : "Unknown User";
                    String courseTitle = review.getCourse() != null ? review.getCourse().getTitle() : "Unknown Course";
                    return mapToResponse(review, userName, courseTitle);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReview(String reviewId, String userId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        String courseId = review.getCourseId();
        reviewRepository.delete(review);

        // Update course statistics
        courseRepository.findById(courseId).ifPresent(this::updateCourseRating);
    }

    private void updateCourseRating(Course course) {
        Double avgRating = reviewRepository.getAverageRatingByCourseId(course.getId());
        Integer count = reviewRepository.countByCourseId(course.getId());

        course.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        course.setReviewCount(count != null ? count : 0);

        courseRepository.save(course);
    }

    private ReviewResponse mapToResponse(Review review, String userName, String courseTitle) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .userName(userName)
                .courseTitle(courseTitle)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
