package course.repository;

import course.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    Page<News> findByIsPublished(Boolean isPublished, Pageable pageable);

    Page<News> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    List<News> findTop8ByIsPublishedTrueOrderByCreatedAtDesc();

    List<News> findTop8ByIsShowHomeTrueOrderByUpdatedAtDesc();

    long countByIsShowHomeTrue();
}
