package course.repository;

import course.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByCourseIdOrderByCreatedAtDesc(String courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") String courseId);

    Integer countByCourseId(String courseId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);
}
