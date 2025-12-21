package course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String content;

    private String thumbnailUrl;

    private Boolean isPublished = false;

    private Boolean isShowHome = false;
}
