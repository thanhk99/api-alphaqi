package course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCompanyResponse {
    private String id;
    private String name;
    private String logo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
