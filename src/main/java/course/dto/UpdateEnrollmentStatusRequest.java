package course.dto;

import course.model.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnrollmentStatusRequest {

    @NotNull(message = "Status is required")
    private EnrollmentStatus status;
}
