package course.repository;

import course.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    List<News> findByIsPublished(Boolean isPublished);

    List<News> findTop8ByIsShowHomeTrueOrderByUpdatedAtDesc();

    long countByIsShowHomeTrue();
}
