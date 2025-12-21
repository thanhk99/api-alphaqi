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
public class ReviewResponse {
    private String id;
    private String userId;
    private String userName;
    private String courseTitle;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
