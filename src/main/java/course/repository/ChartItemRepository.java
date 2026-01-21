package course.repository;

import course.model.ChartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChartItemRepository extends JpaRepository<ChartItem, Long> {
    Optional<ChartItem> findByLabel(String label);
}
