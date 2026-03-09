package course.repository;

import course.model.Article;
import course.model.enums.ArticleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {
    Page<Article> findByIsPublished(Boolean isPublished, Pageable pageable);

    Page<Article> findByType(ArticleType type, Pageable pageable);

    Page<Article> findByIsPublishedAndType(Boolean isPublished, ArticleType type, Pageable pageable);

    Page<Article> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    List<Article> findTop8ByIsPublishedTrueOrderByCreatedAtDesc();
}
