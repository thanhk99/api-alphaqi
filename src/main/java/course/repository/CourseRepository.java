package course.repository;

import course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

        Page<Course> findByIsPublished(Boolean isPublished, Pageable pageable);

        Page<Course> findByCategory(String category, Pageable pageable);

        @Query("SELECT c FROM Course c WHERE " +
                        "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Course> searchCourses(@Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT c FROM Course c WHERE c.isPublished = true AND " +
                        "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Course> searchPublishedCourses(@Param("keyword") String keyword, Pageable pageable);

        List<Course> findTop3ByIsPublishedTrueOrderByCreatedAtDesc();

        List<Course> findTop3ByIsShowHomeTrueOrderByUpdatedAtDesc();

        long countByIsShowHomeTrue();
}
