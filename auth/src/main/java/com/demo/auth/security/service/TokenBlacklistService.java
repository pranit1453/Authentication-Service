package com.demo.auth.security.service;

import com.demo.auth.models.entities.TokenBlacklist;
import com.demo.auth.security.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public void blacklistToken(String token, Instant expiryDate) {
        TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                .token(token)
                .expiryDate(expiryDate)
                .build();
        tokenBlacklistRepository.save(blacklistedToken);
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }

    // Runs every day to clean up expired tokens from the database
    @Scheduled(fixedRate = 86400000)
    public void cleanUpExpiredTokens() {
        tokenBlacklistRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
