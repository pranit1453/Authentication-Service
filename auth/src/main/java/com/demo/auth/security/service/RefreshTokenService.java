package com.demo.auth.security.service;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.models.entities.RefreshToken;
import com.demo.auth.models.entities.User;
import com.demo.auth.security.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {


    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;
    private final RefreshTokenRepository refreshTokenRepository;

    public String create(User user){

        // Raw token sent to client
        String rawToken = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(rawToken)
                .expiry(Instant.now().plusMillis(refreshExpiration))
                .build();

        refreshTokenRepository.save(token);

        return rawToken;
    }

    public User validate(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new AuthException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Refresh token revoked");
        }

        if (refreshToken.isExpired()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        return refreshToken.getUser();
    }

    public void revoke(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED,"Invalid refresh token"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public String rotate(String oldToken){

        RefreshToken refreshToken = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() ->
                        new AuthException(HttpStatus.UNAUTHORIZED,"Invalid refresh token"));

        if(refreshToken.isRevoked())
            throw new AuthException(HttpStatus.FORBIDDEN,"Refresh token revoked");

        if(refreshToken.isExpired())
            throw new AuthException(HttpStatus.UNAUTHORIZED,"Refresh token expired");

        refreshToken.revoke();

        refreshTokenRepository.save(refreshToken);

        return create(refreshToken.getUser());
    }
}
