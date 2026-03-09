package com.demo.auth.service;

import com.demo.auth.generic.ApiResponse;
import com.demo.auth.mapper.AuthMapper;
import com.demo.auth.models.dtos.RefreshTokenRequest;
import com.demo.auth.models.dtos.RefreshTokenResponse;
import com.demo.auth.models.entities.User;
import com.demo.auth.security.service.JwtService;
import com.demo.auth.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenRefreshFacade {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public ApiResponse<RefreshTokenResponse> refreshTokens(RefreshTokenRequest request) {

        // 1. Validate the old token and get user
        User user = refreshTokenService.validate(request.refreshToken());

        // 2. Rotate the refresh token
        String newRefreshToken = refreshTokenService.rotate(request.refreshToken());

        // 3. Generate new access token
        String newAccessToken = jwtService.generateToken(AuthMapper.toAuthUser(user));

        // 4. Wrap and return response
        RefreshTokenResponse responseData = AuthMapper.toRefreshTokenResponse(newAccessToken, newRefreshToken);

        return ApiResponse.<RefreshTokenResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Tokens refreshed successfully")
                .data(responseData)
                .timestamp(Instant.now())
                .build();
    }
}
