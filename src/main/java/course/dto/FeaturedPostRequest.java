package course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedPostRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String thumbnailUrl;

    private Integer priority = 0;

    private Boolean isPublished = false;
}
