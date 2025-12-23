package course.config;

import course.security.JwtAuthenticationEntryPoint;
import course.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/register", "/auth/login/user", "/auth/login/admin", "/auth/refresh")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/courses", "/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/courses/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories", "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/news", "/news/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/articles", "/articles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reviews/**", "/expert-reviews/**", "/partner-companies/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/avatars/**").permitAll()
                        // Public admin registration
                        .requestMatchers(HttpMethod.POST, "/admins").permitAll()

                        // Admin only endpoints
                        .requestMatchers("/admins/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/courses").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/courses/*/lessons").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/lessons/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/news").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/news/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/news/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/articles").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/articles/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/articles/**").hasAuthority("ROLE_ADMIN")

                        // Authenticated endpoints
                        .requestMatchers("/auth/logout", "/auth/me").authenticated()
                        .requestMatchers("/enrollments/**").authenticated()
                        .requestMatchers("/payments/**").authenticated()

                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration
                .setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001", "http://localhost:5173",
                        "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://192.168.1.138:3001",
                        "https://alphaqi.vercel.app", "https://admin-alphaqi.vercel.app", "https://www.alphaqi.vn",
                        "https://admin.alphaqi.vn/"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
