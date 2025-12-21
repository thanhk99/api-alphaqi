package course.model;

import course.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "expert_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertReview {

    @Id
    @Column(length = 8, nullable = false, updatable = false)
    private String id;

    @Column(name = "expert_name", nullable = false, length = 200)
    private String expertName;

    @Column(name = "expert_title", length = 200)
    private String expertTitle;

    @Column(name = "expert_avatar", length = 500)
    private String expertAvatar;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

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
    }
}
