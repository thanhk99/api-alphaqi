package course.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import course.model.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnrollmentResponse {

    // Enrollment info
    private String id;
    private String userId;
    private String courseId;
    private EnrollmentStatus status;
    private Double progress;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;

    // Course overview info
    private CourseOverview course;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CourseOverview {
        private String id;
        private String title;
        private String description;
        private BigDecimal price;
        private String thumbnail;
        private String category;
        private String categoryName;
        private Double averageRating;
        private Integer reviewCount;
        private Integer lessonCount;
    }

}
