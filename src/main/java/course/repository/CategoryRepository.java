package course.repository;

import course.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByName(String name);

    boolean existsByCode(String code);

    java.util.List<Category> findAllByDeletedAtIsNull();

    java.util.Optional<Category> findByCode(String code);
}
