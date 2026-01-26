package course.dto;

import course.model.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
    private Long id;
    private String title;
    private String description;
    private ReportType type;
    private String typeDisplayName;
    private String pdfUrl;
    private String externalUrl;
    private ReportType parentType;
    private String parentTypeDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
