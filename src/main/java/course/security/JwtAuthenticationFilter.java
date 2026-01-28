package course.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Bypass for auth endpoints
        if (path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/refresh")) {
            return true;
        }

        // Bypass for public GET endpoints to avoid unnecessary JWT checks/logs
        if ("GET".equalsIgnoreCase(method)) {
            return path.contains("/news") ||
                    path.contains("/courses") ||
                    path.contains("/categories") ||
                    path.contains("/articles") ||
                    path.contains("/avatars") ||
                    path.contains("/expert-reviews") ||
                    path.contains("/reviews");
        }

        return false;
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                logger.info("JWT found in request");
                try {
                    jwtTokenProvider.validateToken(jwt);

                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    String role = jwtTokenProvider.getRoleFromToken(jwt);
                    logger.info("JWT valid for username: {}, role: {}", username, role);

                    UserDetails userDetails = customUserDetailsService.loadUserByUsernameAndRole(username, role);
                    logger.info("User loaded: {}, Authorities: {}, Enabled: {}, NonLocked: {}",
                            userDetails.getUsername(), userDetails.getAuthorities(),
                            userDetails.isEnabled(), userDetails.isAccountNonLocked());

                    // Check if account is active (not locked)
                    if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Authentication set in SecurityContext for user: {}", username);
                    } else {
                        logger.warn("User account is disabled or locked");
                    }
                } catch (io.jsonwebtoken.MalformedJwtException ex) {
                    logger.error("Invalid JWT token");
                    request.setAttribute("jwt_error", "Invalid JWT token");
                } catch (io.jsonwebtoken.ExpiredJwtException ex) {
                    logger.error("Expired JWT token");
                    request.setAttribute("jwt_error", "Expired JWT token");
                } catch (io.jsonwebtoken.UnsupportedJwtException ex) {
                    logger.error("Unsupported JWT token");
                    request.setAttribute("jwt_error", "Unsupported JWT token");
                } catch (IllegalArgumentException ex) {
                    logger.error("JWT claims string is empty");
                    request.setAttribute("jwt_error", "JWT claims string is empty");
                } catch (io.jsonwebtoken.security.SignatureException ex) {
                    logger.error("Invalid JWT signature");
                    request.setAttribute("jwt_error", "Invalid JWT signature");
                } catch (Exception e) {
                    logger.error("JWT validation failed", e);
                }
            } else {
                logger.info("No JWT found in request");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
