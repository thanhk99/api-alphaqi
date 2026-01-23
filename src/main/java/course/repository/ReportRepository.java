package course.repository;

import course.model.Report;
import course.model.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByTypeIn(java.util.List<ReportType> types, Pageable pageable);

    Page<Report> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Report> findByTypeInAndTitleContainingIgnoreCase(java.util.List<ReportType> types, String title,
            Pageable pageable);

    @Query("SELECT MAX(r.updatedAt) FROM Report r")
    LocalDateTime findLatestUpdatedAt();

    @Query("SELECT MAX(r.updatedAt) FROM Report r WHERE r.type = :type")
    LocalDateTime findLatestUpdatedAtByType(@Param("type") ReportType type);
}
