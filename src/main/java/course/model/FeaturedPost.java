package course.model;

import course.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "featured_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedPost {

    @Id
    @Column(length = 8, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String thumbnail;

    @Column(name = "html_url", length = 500)
    private String htmlUrl;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = IdGenerator.generateId();
        }
        if (this.isPublished == null) {
            this.isPublished = false;
        }
        if (this.priority == null) {
            this.priority = 0;
        }
    }
}
