package course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertReviewResponse {
    private String id;
    private String expertName;
    private String expertTitle;
    private String expertAvatar;
    private String content;
    private Integer rating;
    private String courseId;
    private String courseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
