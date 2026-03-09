package com.demo.auth.models.dtos;

public record RefreshTokenResponse(
        String accessToken,

        String refreshToken,

        String tokenType
) {}
