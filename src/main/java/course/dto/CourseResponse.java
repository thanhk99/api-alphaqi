package course.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResponse {
    private String id;
    private String code;
    private String title;
    private String description;
    private BigDecimal price;
    private String thumbnail;
    private String category;
    private String categoryName;
    private Boolean isPublished;
    private Boolean isShowHome;
    private String introVideoUrl;
    private Double averageRating;
    private Integer reviewCount;
    private Integer lessonCount;
    private Integer enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEnrolled;
    private List<LessonResponse> lessons; // Changed to List and added import
}
