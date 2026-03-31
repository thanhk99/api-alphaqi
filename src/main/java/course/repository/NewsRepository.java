package course.repository;

import course.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import course.dto.projection.NewsArticleProjection;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    Page<News> findByIsPublished(Boolean isPublished, Pageable pageable);

    Page<News> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    List<News> findTop8ByIsPublishedTrueOrderByCreatedAtDesc();

    List<News> findTop8ByIsShowHomeTrueOrderByUpdatedAtDesc();

    long countByIsShowHomeTrue();

    @Query(value = "SELECT * FROM ( " +
                   "SELECT id, title, description, thumbnail, is_published as isPublished, is_show_home as isShowHome, created_at as createdAt, updated_at as updatedAt, 'NEWS' as type FROM news " +
                   "UNION ALL " +
                   "SELECT id, title, description, thumbnail, is_published as isPublished, false as isShowHome, created_at as createdAt, updated_at as updatedAt, type FROM articles " +
                   ") as tempResult \n-- #pageable\n",
           countQuery = "SELECT count(*) FROM (SELECT id FROM news UNION ALL SELECT id FROM articles) as tempCount",
           nativeQuery = true)
    Page<NewsArticleProjection> findAllNewsAndArticles(Pageable pageable);

    @Query(value = "SELECT * FROM ( " +
                   "SELECT id, title, description, thumbnail, is_published as isPublished, is_show_home as isShowHome, created_at as createdAt, updated_at as updatedAt, 'NEWS' as type FROM news WHERE is_published = true " +
                   "UNION ALL " +
                   "SELECT id, title, description, thumbnail, is_published as isPublished, false as isShowHome, created_at as createdAt, updated_at as updatedAt, type FROM articles WHERE is_published = true " +
                   ") as tempResult \n-- #pageable\n",
           countQuery = "SELECT count(*) FROM (SELECT id FROM news WHERE is_published = true UNION ALL SELECT id FROM articles WHERE is_published = true) as tempCount",
           nativeQuery = true)
    Page<NewsArticleProjection> findPublishedNewsAndArticles(Pageable pageable);

    @Query(value = "SELECT * FROM ( " +
                   "SELECT id, title, description, thumbnail, is_published as isPublished, is_show_home as isShowHome, created_at as createdAt, updated_at as updatedAt, 'NEWS' as type FROM news WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   "UNION ALL " +
                   "SELECT id, title, description, thumbnail, is_published as isPublished, false as isShowHome, created_at as createdAt, updated_at as updatedAt, type FROM articles WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                   ") as tempResult \n-- #pageable\n",
           countQuery = "SELECT count(*) FROM (" +
                        "SELECT id FROM news WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "UNION ALL " +
                        "SELECT id FROM articles WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                        ") as tempCount",
           nativeQuery = true)
    Page<NewsArticleProjection> searchNewsAndArticles(@Param("keyword") String keyword, Pageable pageable);
}
