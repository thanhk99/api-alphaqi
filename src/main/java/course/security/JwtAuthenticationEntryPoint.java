package course.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import course.util.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = "Unauthorized";
        // Check if we have a specific error message from the filter
        if (request.getAttribute("jwt_error") != null) {
            message = (String) request.getAttribute("jwt_error");
        } else if (authException != null) {
            message = authException.getMessage();
        }

        ApiResponse<Void> apiResponse = ApiResponse.error(message);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
