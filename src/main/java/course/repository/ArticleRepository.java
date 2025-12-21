package course.repository;

import course.model.Article;
import course.model.enums.ArticleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {
    List<Article> findByIsPublished(Boolean isPublished);

    List<Article> findByType(ArticleType type);

    List<Article> findByIsPublishedAndType(Boolean isPublished, ArticleType type);
}
