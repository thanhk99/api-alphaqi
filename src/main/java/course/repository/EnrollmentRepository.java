package course.repository;

import course.model.Enrollment;
import course.model.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c WHERE e.userId = :userId")
    Page<Enrollment> findByUserIdWithCourse(@Param("userId") String userId, Pageable pageable);

    List<Enrollment> findByUserId(String userId);

    List<Enrollment> findByCourseId(String courseId);

    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    // Check if user has active enrollment (ACTIVE or COMPLETED, not CANCELLED or
    // PENDING)
    boolean existsByUserIdAndCourseIdAndStatusIn(String userId, String courseId, List<EnrollmentStatus> statuses);

    Page<Enrollment> findByStatus(EnrollmentStatus status, Pageable pageable);

    Long countByCourseId(String courseId);

    // Admin queries
    @Query(value = "SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.user u", countQuery = "SELECT count(e) FROM Enrollment e")
    Page<Enrollment> findAllWithCourseAndUser(Pageable pageable);

    @Query(value = "SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.user u WHERE e.courseId = :courseId", countQuery = "SELECT count(e) FROM Enrollment e WHERE e.courseId = :courseId")
    Page<Enrollment> findByCourseIdWithCourseAndUser(@Param("courseId") String courseId, Pageable pageable);

    @Query(value = "SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.user u WHERE e.status = :status", countQuery = "SELECT count(e) FROM Enrollment e WHERE e.status = :status")
    Page<Enrollment> findByStatusWithCourseAndUser(@Param("status") EnrollmentStatus status, Pageable pageable);

    @Query(value = "SELECT e FROM Enrollment e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.user u " +
            "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT count(e) FROM Enrollment e LEFT JOIN e.user u LEFT JOIN e.course c " +
            "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Enrollment> searchEnrollments(@Param("keyword") String keyword, Pageable pageable);

    Long countByStatus(EnrollmentStatus status);
}
