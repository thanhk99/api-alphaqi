package course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentStatisticsResponse {
    private Long totalEnrollments;
    private Long activeEnrollments;
    private Long completedEnrollments;
    private Long cancelledEnrollments;
    private Long pendingEnrollments;
}
