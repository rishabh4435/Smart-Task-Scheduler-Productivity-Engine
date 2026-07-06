package com.thakur.scheduler.task.job;

import com.thakur.scheduler.task.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;


    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("🧹 Starting midnight cleanup of expired refresh tokens...");
        long deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());

        log.info("✅ Cleanup complete. Permanently deleted {} expired tokens from MongoDB.", deletedCount);
    }
}