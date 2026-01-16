package course.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import course.model.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminEnrollmentResponse {

    // Enrollment info
    private String id;
    private String userId;
    private String courseId;
    private EnrollmentStatus status;
    private Double progress;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;

    // User info
    private UserInfo user;

    // Course info
    private CourseInfo course;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String phoneNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CourseInfo {
        private String id;
        private String title;
        private String category;
        private String categoryName;

    }
}
