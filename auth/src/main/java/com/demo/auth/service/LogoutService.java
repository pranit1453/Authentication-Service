package com.demo.auth.service;

import com.demo.auth.generic.ApiResponse;
import com.demo.auth.models.dtos.LogoutRequest;
import com.demo.auth.security.service.JwtService;
import com.demo.auth.security.service.RefreshTokenService;
import com.demo.auth.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    public ApiResponse<String> logout(LogoutRequest request, String authHeader) {

        // 1. Revoke the refresh token so no new access tokens can be generated
        refreshTokenService.revoke(request.refreshToken());

        // 2. Blacklist the current access token if present
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);

            // Only blacklist if it's currently valid (hasn't expired naturally yet)
            if (jwtService.validateToken(accessToken)) {
                Date expirationDate = jwtService.getExpirationDate(accessToken);
                tokenBlacklistService.blacklistToken(accessToken, expirationDate.toInstant());
            }
        }

        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("User logged out successfully")
                .data(null)
                .timestamp(Instant.now())
                .build();
    }
}
