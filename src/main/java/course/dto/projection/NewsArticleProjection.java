package course.dto.projection;

import java.time.LocalDateTime;

public interface NewsArticleProjection {
    String getId();
    String getTitle();
    String getDescription();
    String getThumbnail();
    Boolean getIsPublished();
    Boolean getIsShowHome();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    String getType();
}
