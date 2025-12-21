package course.security;

import course.model.RefreshToken;
import course.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class RefreshTokenProvider {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public RefreshTokenProvider(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Generate and save refresh token
     */
    @Transactional
    public String generateRefreshToken(String userId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);

        refreshTokenRepository.save(refreshToken);

        return token;
    }

    /**
     * Validate refresh token and return it if valid
     */
    public RefreshToken validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isExpired())
                .orElse(null);
    }

    /**
     * Delete refresh token (logout)
     */
    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    /**
     * Delete all refresh tokens for a user
     */
    @Transactional
    public void deleteAllUserRefreshTokens(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Cleanup expired tokens (can be scheduled)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
