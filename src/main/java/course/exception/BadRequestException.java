package course.exception;

import lombok.Getter;
import java.util.List;

@Getter
public class BadRequestException extends RuntimeException {
    private List<String> details;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, List<String> details) {
        super(message);
        this.details = details;
    }
}
