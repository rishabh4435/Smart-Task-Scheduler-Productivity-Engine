package com.thakur.scheduler.task.service;

import com.thakur.scheduler.task.exception.UnauthorizedException;
import com.thakur.scheduler.task.model.RefreshToken;
import com.thakur.scheduler.task.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private static final int MAX_ACTIVE_DEVICES = 5;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public RefreshToken createRefreshToken(String userId) {


        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalseOrderByExpiryDateAsc(userId);

        if (activeTokens.size() >= MAX_ACTIVE_DEVICES) {

            revokeToken(activeTokens.getFirst());
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(generateSecureToken())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .createdAt(Instant.now())
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.isRevoked()) {
            revokeAllUserTokens(token.getUserId());
            throw new UnauthorizedException("Security Alert: Revoked token used! All your sessions have been terminated.");
        }

        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            revokeToken(token);
            throw new UnauthorizedException("Refresh token was expired. Please sign in again.");
        }
        return token;
    }

    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllUserTokens(String userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalseOrderByExpiryDateAsc(userId);
        activeTokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(activeTokens);
    }


    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}