package course.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsResponse {
    private String id;
    private String title;
    private String description;
    private String content;
    private String thumbnail;
    private Boolean isPublished;
    private Boolean isShowHome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
