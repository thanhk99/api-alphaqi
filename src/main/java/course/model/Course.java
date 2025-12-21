package course.model;

import course.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @Column(length = 8, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(unique = true, length = 20)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String thumbnail;

    @Column(length = 100)
    private String category;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "intro_video_url", length = 500)
    private String introVideoUrl;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "course_lessons", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "lesson_id"))
    private List<Lesson> lessons = new ArrayList<>();

    @Column(name = "is_show_home")
    private Boolean isShowHome = false;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = IdGenerator.generateId();
        }
        if (this.code == null) {
            this.code = IdGenerator.generateCourseCode();
        }
        if (this.isPublished == null) {
            this.isPublished = false;
        }
        if (this.averageRating == null) {
            this.averageRating = 0.0;
        }
        if (this.reviewCount == null) {
            this.reviewCount = 0;
        }
    }

    public boolean isRegistrationOpen() {
        return true;
    }

    public int getCurrentStudents() {
        if (this.enrollments == null) {
            return 0;
        }
        return (int) this.enrollments.stream()
                .filter(e -> e.getStatus() == course.model.enums.EnrollmentStatus.ACTIVE ||
                        e.getStatus() == course.model.enums.EnrollmentStatus.PENDING)
                .count();
    }

}
