package course.repository;

import course.model.Enrollment;
import course.model.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    List<Enrollment> findByUserId(String userId);

    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c WHERE e.userId = :userId")
    List<Enrollment> findByUserIdWithCourse(@Param("userId") String userId);

    List<Enrollment> findByCourseId(String courseId);

    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    // Check if user has active enrollment (ACTIVE or COMPLETED, not CANCELLED or
    // PENDING)
    boolean existsByUserIdAndCourseIdAndStatusIn(String userId, String courseId, List<EnrollmentStatus> statuses);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    Long countByCourseId(String courseId);

    // Admin queries
    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.user u")
    List<Enrollment> findAllWithCourseAndUser();

    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.user u WHERE e.courseId = :courseId")
    List<Enrollment> findByCourseIdWithCourseAndUser(@Param("courseId") String courseId);

    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.user u WHERE e.status = :status")
    List<Enrollment> findByStatusWithCourseAndUser(@Param("status") EnrollmentStatus status);

    Long countByStatus(EnrollmentStatus status);
}
