package course.controller;

import course.dto.*;
import course.service.AuthService;
import course.util.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.registerUser(request);

        // Set refresh token as HttpOnly cookie
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login/user")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.loginUser(request);

        // Set refresh token as HttpOnly cookie
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/login/admin")
    public ResponseEntity<ApiResponse<AuthResponse>> loginAdmin(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.loginAdmin(request);

        // Set refresh token as HttpOnly cookie
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        // Try to get refresh token from cookie first, then from request body
        String refreshToken = cookieRefreshToken != null ? cookieRefreshToken
                : (request != null ? request.getRefreshToken() : null);

        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refresh token is required"));
        }

        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);

        // Update refresh token cookie
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        // Try to get refresh token from cookie first, then from request body
        String refreshToken = cookieRefreshToken != null ? cookieRefreshToken
                : (request != null ? request.getRefreshToken() : null);

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // Clear refresh token cookie
        clearRefreshTokenCookie(response);

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    /**
     * Set refresh token as HttpOnly cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/api/auth");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    /**
     * Clear refresh token cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
