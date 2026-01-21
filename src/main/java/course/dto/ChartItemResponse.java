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
public class ChartItemResponse {
    private Long id;
    private String label;
    private Double value;
    private LocalDateTime updatedAt;
}
