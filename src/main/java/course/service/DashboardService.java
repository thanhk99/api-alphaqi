package course.service;

import course.dto.DashboardStatsResponse;
import course.model.enums.EnrollmentStatus;
import course.repository.CourseRepository;
import course.repository.EnrollmentRepository;
import course.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DashboardService(UserRepository userRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalCourses(courseRepository.count())
                .activeEnrollments(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE))
                .pendingReviews(enrollmentRepository.countByStatus(EnrollmentStatus.PENDING))
                .build();
    }
}
