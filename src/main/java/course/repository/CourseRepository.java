package course.repository;

import course.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

        List<Course> findByIsPublished(Boolean isPublished);

        List<Course> findByCategory(String category);

        @Query("SELECT c FROM Course c WHERE " +
                        "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Course> searchCourses(@Param("keyword") String keyword);

        @Query("SELECT c FROM Course c WHERE c.isPublished = true AND " +
                        "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        List<Course> searchPublishedCourses(@Param("keyword") String keyword);

        List<Course> findTop3ByIsShowHomeTrueOrderByUpdatedAtDesc();

        long countByIsShowHomeTrue();
}
