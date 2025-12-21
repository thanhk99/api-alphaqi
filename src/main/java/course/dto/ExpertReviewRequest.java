package course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertReviewRequest {

    @NotBlank(message = "Tên chuyên gia là bắt buộc")
    private String expertName;

    private String expertTitle;

    @NotBlank(message = "Nội dung đánh giá là bắt buộc")
    private String content;

    @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
    private Integer rating;

    private String courseId;

    private String expertAvatarUrl;
}
