package course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCompanyRequest {

    @NotBlank(message = "Tên công ty là bắt buộc")
    private String name;

    private String logoUrl;
}
