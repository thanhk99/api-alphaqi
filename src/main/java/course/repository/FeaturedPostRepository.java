package course.repository;

import course.model.FeaturedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedPostRepository extends JpaRepository<FeaturedPost, String> {
    
    List<FeaturedPost> findAllByOrderByCreatedAtDesc();
    
    Page<FeaturedPost> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);
    
    List<FeaturedPost> findTop10ByIsPublishedTrueOrderByCreatedAtDesc();
}
