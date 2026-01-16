package course.service;

import course.dto.AdminEnrollmentResponse;
import course.dto.EnrollmentRequest;
import course.dto.EnrollmentResponse;
import course.dto.EnrollmentStatisticsResponse;

import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.repository.CourseRepository;
import course.repository.EnrollmentRepository;
import course.repository.CategoryRepository;
import course.model.Category;

import course.repository.UserRepository;
import course.model.Course;
import course.model.Enrollment;
import course.model.User;
import course.model.enums.EnrollmentStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Enrollment enrollCourse(String userId, EnrollmentRequest request) {
        // Check if user exists in the users table (only students/users can enroll)
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Chỉ tài khoản người dùng bình thường mới có thể đăng ký khóa học.");
        }

        // Check if course exists
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khóa học."));

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, request.getCourseId())) {
            throw new BadRequestException("Bạn đã đăng ký khóa học này rồi.");
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(request.getCourseId());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setProgress(0.0);

        return enrollmentRepository.save(enrollment);
    }

    public List<EnrollmentResponse> getMyEnrollments(String userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdWithCourse(userId);

        return enrollments.stream()
                .map(this::mapToEnrollmentResponse)
                .toList();
    }

    public boolean checkEnrollment(String userId, String courseId) {
        // Only consider ACTIVE or COMPLETED enrollments as "enrolled"
        // CANCELLED and PENDING should return false
        List<EnrollmentStatus> activeStatuses = List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED);
        return enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(userId, courseId, activeStatuses);
    }

    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        Course course = enrollment.getCourse();

        EnrollmentResponse.CourseOverview courseOverview = EnrollmentResponse.CourseOverview.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .thumbnail(course.getThumbnail())
                .category(course.getCategory())
                .categoryName(course.getCategory() != null
                        ? categoryRepository.findByCode(course.getCategory()).map(Category::getName).orElse(null)
                        : null)
                .build();

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus())
                .progress(enrollment.getProgress())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .course(courseOverview)
                .build();
    }

    @Transactional
    public Enrollment updateProgress(String enrollmentId, Double progress) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        enrollment.setProgress(progress);

        // Mark as completed if progress is 100%
        if (progress >= 100.0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void cancelEnrollment(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    // Admin methods
    public List<AdminEnrollmentResponse> getAllEnrollmentsForAdmin() {
        List<Enrollment> enrollments = enrollmentRepository.findAllWithCourseAndUser();
        return enrollments.stream()
                .map(this::mapToAdminEnrollmentResponse)
                .toList();
    }

    public List<AdminEnrollmentResponse> getEnrollmentsByCourseForAdmin(String courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithCourseAndUser(courseId);
        return enrollments.stream()
                .map(this::mapToAdminEnrollmentResponse)
                .toList();
    }

    public List<AdminEnrollmentResponse> getEnrollmentsByStatusForAdmin(EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentRepository.findByStatusWithCourseAndUser(status);
        return enrollments.stream()
                .map(this::mapToAdminEnrollmentResponse)
                .toList();
    }

    public EnrollmentStatisticsResponse getEnrollmentStatistics() {
        Long total = enrollmentRepository.count();
        Long active = enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE);
        Long completed = enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED);
        Long cancelled = enrollmentRepository.countByStatus(EnrollmentStatus.CANCELLED);
        Long pending = enrollmentRepository.countByStatus(EnrollmentStatus.PENDING);

        return EnrollmentStatisticsResponse.builder()
                .totalEnrollments(total)
                .activeEnrollments(active)
                .completedEnrollments(completed)
                .cancelledEnrollments(cancelled)
                .pendingEnrollments(pending)
                .build();
    }

    @Transactional
    public Enrollment updateEnrollmentStatusByAdmin(String enrollmentId, EnrollmentStatus newStatus) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        enrollment.setStatus(newStatus);

        // If status is set to COMPLETED, set completedAt timestamp
        if (newStatus == EnrollmentStatus.COMPLETED && enrollment.getCompletedAt() == null) {
            enrollment.setCompletedAt(LocalDateTime.now());
        }

        // If status is changed from COMPLETED to something else, clear completedAt
        if (newStatus != EnrollmentStatus.COMPLETED && enrollment.getCompletedAt() != null) {
            enrollment.setCompletedAt(null);
        }

        return enrollmentRepository.save(enrollment);
    }

    private AdminEnrollmentResponse mapToAdminEnrollmentResponse(Enrollment enrollment) {
        Course courseEntity = enrollment.getCourse();
        User userEntity = enrollment.getUser();

        // Map user info
        AdminEnrollmentResponse.UserInfo userInfo = AdminEnrollmentResponse.UserInfo.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .fullName(userEntity.getFullName())
                .phoneNumber(userEntity.getPhoneNumber())
                .build();

        // Map course info
        AdminEnrollmentResponse.CourseInfo courseInfo = AdminEnrollmentResponse.CourseInfo.builder()
                .id(courseEntity.getId())
                .title(courseEntity.getTitle())
                .category(courseEntity.getCategory())
                .categoryName(courseEntity.getCategory() != null
                        ? categoryRepository.findByCode(courseEntity.getCategory()).map(Category::getName).orElse(null)
                        : null)
                .build();

        return AdminEnrollmentResponse.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus())
                .progress(enrollment.getProgress())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .user(userInfo)
                .course(courseInfo)
                .build();
    }

}
