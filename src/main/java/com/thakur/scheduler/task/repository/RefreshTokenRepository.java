package com.thakur.scheduler.task.repository;

import com.thakur.scheduler.task.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(String userId);
    List<RefreshToken> findByUserIdAndRevokedFalseOrderByExpiryDateAsc(String userId);
    long deleteByExpiryDateBefore(Instant now);
}