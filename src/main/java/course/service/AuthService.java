package course.service;

import course.dto.AuthRequest;
import course.dto.AuthResponse;
import course.dto.RegisterRequest;
import course.exception.BadRequestException;
import course.exception.UnauthorizedException;
import course.model.Admin;
import course.model.RefreshToken;
import course.model.User;
import course.repository.AdminRepository;
import course.repository.UserRepository;
import course.security.JwtTokenProvider;
import course.security.RefreshTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final AdminRepository adminRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final RefreshTokenProvider refreshTokenProvider;
        private final AuthenticationManager authenticationManager;

        public AuthService(UserRepository userRepository,
                        AdminRepository adminRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        RefreshTokenProvider refreshTokenProvider,
                        AuthenticationManager authenticationManager) {
                this.userRepository = userRepository;
                this.adminRepository = adminRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtTokenProvider = jwtTokenProvider;
                this.refreshTokenProvider = refreshTokenProvider;
                this.authenticationManager = authenticationManager;
        }

        @Transactional
        public AuthResponse registerUser(RegisterRequest request) {
                // Check if username and email exist
                java.util.List<String> errors = new java.util.ArrayList<>();
                if (userRepository.existsByUsername(request.getUsername())) {
                        errors.add("username: Tên đăng nhập đã tồn tại.");
                }
                if (userRepository.existsByEmail(request.getEmail())) {
                        errors.add("email: Email đã tồn tại.");
                }

                if (!errors.isEmpty()) {
                        throw new BadRequestException("Dữ liệu đăng ký đã tồn tại.", errors);
                }

                // Create new user
                User user = new User();
                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());
                user.setPhoneNumber(request.getPhoneNumber());

                user = userRepository.save(user);

                // Generate tokens
                String accessToken = jwtTokenProvider.generateAccessToken(
                                user.getId(), user.getUsername(), "ROLE_USER");
                String refreshToken = refreshTokenProvider.generateRefreshToken(user.getId());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .userId(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role("USER")
                                .membershipLevel(user.getMembershipLevel().name())
                                .status(user.getStatus().name())
                                .build();
        }

        @Transactional
        public AuthResponse loginUser(AuthRequest request) {
                // Authenticate
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername(),
                                                request.getPassword()));

                // Get user
                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new UnauthorizedException(
                                                "Tên đăng nhập hoặc mật khẩu không chính xác."));

                // Generate tokens
                String accessToken = jwtTokenProvider.generateAccessToken(
                                user.getId(), user.getUsername(), "ROLE_USER");
                String refreshToken = refreshTokenProvider.generateRefreshToken(user.getId());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .userId(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role("USER")
                                .membershipLevel(user.getMembershipLevel().name())
                                .status(user.getStatus().name())
                                .build();
        }

        @Transactional
        public AuthResponse loginAdmin(AuthRequest request) {
                // Authenticate
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getUsername(),
                                                request.getPassword()));

                // Get admin
                Admin admin = adminRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new UnauthorizedException(
                                                "Tên đăng nhập hoặc mật khẩu không chính xác."));

                // Generate tokens
                String accessToken = jwtTokenProvider.generateAccessToken(
                                admin.getId(), admin.getUsername(), "ROLE_ADMIN");
                String refreshToken = refreshTokenProvider.generateRefreshToken(admin.getId());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .userId(admin.getId())
                                .username(admin.getUsername())
                                .email(admin.getEmail())
                                .role("ADMIN")
                                .status(admin.getStatus().name())
                                .build();
        }

        @Transactional
        public AuthResponse refreshAccessToken(String refreshTokenString) {
                RefreshToken refreshToken = refreshTokenProvider.validateRefreshToken(refreshTokenString);

                if (refreshToken == null) {
                        throw new UnauthorizedException("Mã làm mới không hợp lệ hoặc đã hết hạn");
                }

                String userId = refreshToken.getUserId();

                // Try to find user first
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                        String accessToken = jwtTokenProvider.generateAccessToken(
                                        user.getId(), user.getUsername(), "ROLE_USER");

                        return AuthResponse.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(refreshTokenString)
                                        .userId(user.getId())
                                        .username(user.getUsername())
                                        .email(user.getEmail())
                                        .role("USER")
                                        .membershipLevel(user.getMembershipLevel().name())
                                        .status(user.getStatus().name())
                                        .build();
                }

                // Try admin
                Admin admin = adminRepository.findById(userId)
                                .orElseThrow(() -> new UnauthorizedException("Không tìm thấy người dùng"));

                String accessToken = jwtTokenProvider.generateAccessToken(
                                admin.getId(), admin.getUsername(), "ROLE_ADMIN");

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshTokenString)
                                .userId(admin.getId())
                                .username(admin.getUsername())
                                .email(admin.getEmail())
                                .role("ADMIN")
                                .status(admin.getStatus().name())
                                .build();
        }

        @Transactional
        public void logout(String refreshToken) {
                refreshTokenProvider.deleteRefreshToken(refreshToken);
        }
}
