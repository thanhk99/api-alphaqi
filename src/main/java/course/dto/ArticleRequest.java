package course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    private String description;

    private String content; // Optional, for INTERNAL articles

    private String link; // Optional, for EXTERNAL articles

    @NotBlank(message = "Type is required (INTERNAL or EXTERNAL)")
    private String type;

    private String thumbnailUrl;

    private Boolean isPublished = false;
}
