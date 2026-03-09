package course.repository;

import course.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    Page<Review> findByCourseIdOrderByCreatedAtDesc(String courseId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId")
    Double getAverageRatingByCourseId(@Param("courseId") String courseId);

    Integer countByCourseId(String courseId);

    boolean existsByUserIdAndCourseId(String userId, String courseId);
}
