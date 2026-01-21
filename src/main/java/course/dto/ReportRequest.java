package course.dto;

import course.model.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {
    private String title;
    private String description;
    private ReportType type;
    private String pdfUrl;
    private String externalLink;
}
