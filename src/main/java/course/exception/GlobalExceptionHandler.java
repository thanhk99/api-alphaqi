package course.exception;

import course.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Không tìm thấy",
                                ex.getMessage(),
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(
                        BadRequestException ex, WebRequest request) {
                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Yêu cầu không hợp lệ")
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .details(ex.getDetails())
                                .build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        UnauthorizedException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Không có quyền truy cập",
                                ex.getMessage(),
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Bị từ chối truy cập",
                                "Bạn không có quyền truy cập tài nguyên này",
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Đăng nhập thất bại",
                                "Tên đăng nhập hoặc mật khẩu không chính xác",
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ErrorResponse> handleDisabledException(
                        DisabledException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Tài khoản bị vô hiệu hóa",
                                "Tài khoản của bạn chưa được kích hoạt hoặc đã bị vô hiệu hóa",
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(LockedException.class)
        public ResponseEntity<ErrorResponse> handleLockedException(
                        LockedException ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Tài khoản bị khóa",
                                "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên",
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex, WebRequest request) {
                List<String> details = new ArrayList<>();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        details.add(error.getField() + ": " + error.getDefaultMessage());
                }

                ErrorResponse error = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Dữ liệu không hợp lệ")
                                .message("Vui lòng kiểm tra lại các thông tin đã nhập")
                                .path(request.getDescription(false).replace("uri=", ""))
                                .details(details)
                                .build();

                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
                        org.springframework.web.multipart.MaxUploadSizeExceededException ex, WebRequest request) {

                String limitInfo = "N/A";
                if (ex.getMaxUploadSize() > 0) {
                        limitInfo = (ex.getMaxUploadSize() / (1024 * 1024)) + "MB (" + ex.getMaxUploadSize()
                                        + " bytes)";
                } else {
                        // In some cases (e.g. Tomcat level), maxUploadSize might be -1
                        // Try to get it from property if possible, or just state it's exceeded
                        limitInfo = "500MB (Cấu hình)";
                }

                String actualSize = request.getHeader("Content-Length");
                String detailedMessage = String.format(
                                "File tải lên vượt quá giới hạn. Giới hạn: %s. Dung lượng gửi lên (Content-Length): %s bytes.",
                                limitInfo, (actualSize != null ? actualSize : "Không xác định"));

                System.out.println("DEBUG UPLOAD ERROR: " + detailedMessage);

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "File quá lớn",
                                detailedMessage,
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex, WebRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Lỗi hệ thống nội bộ",
                                ex.getMessage(),
                                request.getDescription(false).replace("uri=", ""));
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
