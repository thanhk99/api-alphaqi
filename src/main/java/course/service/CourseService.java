package course.service;

import course.dto.CourseRequest;
import course.dto.CourseResponse;
import course.dto.LessonResponse;
import course.dto.UploadResponse;
import course.util.HtmlSanitizer;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.Course;

import course.repository.CourseRepository;
import course.repository.EnrollmentRepository;
import course.repository.LessonRepository;
import course.repository.CategoryRepository;
import course.model.Category;
import course.model.Enrollment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import course.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    public CourseService(CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            LessonRepository lessonRepository,
            CategoryRepository categoryRepository,
            CloudinaryService cloudinaryService) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request, MultipartFile introVideo, MultipartFile thumbnail) {
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(HtmlSanitizer.sanitize(request.getDescription()));
        course.setPrice(request.getPrice());
        course.setCategory(request.getCategory());
        course.setCategory(request.getCategory());

        // Upload thumbnail if provided
        if (thumbnail != null && !thumbnail.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
            course.setThumbnail(uploadResponse.getUrl());
        } else {
            course.setThumbnail(request.getThumbnailUrl());
        }

        // Upload intro video if provided
        if (introVideo != null && !introVideo.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadVideo(introVideo);
            course.setIntroVideoUrl(uploadResponse.getUrl());
        } else {
            course.setIntroVideoUrl(request.getIntroVideoUrl());
        }

        course.setIsPublished(request.getIsPublished());

        // Handle isShowHome with validation
        if (request.getIsShowHome() != null && request.getIsShowHome()) {
            validateIsShowHomeLimit();
            course.setIsShowHome(true);
        } else {
            course.setIsShowHome(false);
        }

        if (request.getLessonIds() != null && !request.getLessonIds().isEmpty()) {
            List<course.model.Lesson> lessons = lessonRepository.findAllById(request.getLessonIds());
            course.setLessons(lessons);
        }

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    @Transactional
    public CourseResponse updateCourse(String id, CourseRequest request, MultipartFile introVideo,
            MultipartFile thumbnail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.setTitle(request.getTitle());
        course.setDescription(HtmlSanitizer.sanitize(request.getDescription()));
        course.setPrice(request.getPrice());
        course.setCategory(request.getCategory());

        // Upload thumbnail if provided
        if (thumbnail != null && !thumbnail.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadImage(thumbnail);
            course.setThumbnail(uploadResponse.getUrl());
        } else if (request.getThumbnailUrl() != null) {
            course.setThumbnail(request.getThumbnailUrl());
        }

        // Upload intro video if provided
        if (introVideo != null && !introVideo.isEmpty()) {
            UploadResponse uploadResponse = cloudinaryService.uploadVideo(introVideo);
            course.setIntroVideoUrl(uploadResponse.getUrl());
        } else if (request.getIntroVideoUrl() != null) {
            course.setIntroVideoUrl(request.getIntroVideoUrl());
        }

        course.setIsPublished(request.getIsPublished());

        // Handle isShowHome with validation
        if (request.getIsShowHome() != null && request.getIsShowHome()) {
            // Only validate if it wasn't already true
            if (!Boolean.TRUE.equals(course.getIsShowHome())) {
                validateIsShowHomeLimit();
            }
            course.setIsShowHome(true);
        } else {
            course.setIsShowHome(false);
        }

        if (request.getLessonIds() != null) {
            List<course.model.Lesson> lessons = lessonRepository.findAllById(request.getLessonIds());
            course.setLessons(lessons);
        }

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    @Transactional
    public void deleteCourse(String id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    public CourseResponse getCourseById(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        return mapToResponse(course);
    }

    public List<CourseResponse> getAllCourses() {
        return mapToResponseList(courseRepository.findAll());
    }

    public List<CourseResponse> getPublishedCourses() {
        return mapToResponseList(courseRepository.findByIsPublished(true));
    }

    public List<CourseResponse> searchCourses(String keyword) {
        return mapToResponseList(courseRepository.searchPublishedCourses(keyword));
    }

    public List<CourseResponse> filterCourses(String category) {
        List<Course> courses;

        if (category != null && !category.isEmpty()) {
            courses = courseRepository.findByCategory(category);
        } else {
            courses = courseRepository.findByIsPublished(true);
        }

        return mapToResponseList(courses);
    }

    public List<CourseResponse> getHomeCourses() {
        return mapToResponseList(courseRepository.findTop3ByIsShowHomeTrueOrderByUpdatedAtDesc());
    }

    private void validateIsShowHomeLimit() {
        if (courseRepository.countByIsShowHomeTrue() >= 3) {
            throw new BadRequestException("Only 3 courses can be shown on home page");
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        return null;
    }

    private Set<String> getUserEnrolledCourseIds(String userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        return enrollmentRepository.findByUserId(userId).stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());
    }

    private List<CourseResponse> mapToResponseList(List<Course> courses) {
        String userId = getCurrentUserId();
        Set<String> enrolledCourseIds = getUserEnrolledCourseIds(userId);
        return courses.stream()
                .map(course -> mapToResponse(course, enrolledCourseIds))
                .collect(Collectors.toList());
    }

    private CourseResponse mapToResponse(Course course) {
        String userId = getCurrentUserId();
        Set<String> enrolledCourseIds = getUserEnrolledCourseIds(userId);
        return mapToResponse(course, enrolledCourseIds);
    }

    private CourseResponse mapToResponse(Course course, Set<String> enrolledCourseIds) {
        boolean isEnrolled = enrolledCourseIds.contains(course.getId());
        int enrollmentCount = enrollmentRepository.countByCourseId(course.getId()).intValue();

        CourseResponse.CourseResponseBuilder builder = CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .thumbnail(course.getThumbnail())
                .introVideoUrl(course.getIntroVideoUrl())
                .category(course.getCategory())
                .categoryName(course.getCategory() != null
                        ? categoryRepository.findByCode(course.getCategory()).map(Category::getName).orElse(null)
                        : null)
                .isPublished(course.getIsPublished())
                .isShowHome(course.getIsShowHome())
                .averageRating(course.getAverageRating())
                .reviewCount(course.getReviewCount())
                .lessonCount(course.getLessons() != null ? course.getLessons().size() : 0)
                .enrollmentCount(enrollmentCount)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .isEnrolled(isEnrolled)
                .lessons(course.getLessons() != null ? course.getLessons().stream()
                        .map(l -> LessonResponse.builder()
                                .id(l.getId())
                                .title(l.getTitle())
                                .content(l.getContent())
                                .createdAt(l.getCreatedAt())
                                .updatedAt(l.getUpdatedAt())
                                .build())
                        .collect(Collectors.toList()) : Collections.emptyList());

        return builder.build();
    }
}
