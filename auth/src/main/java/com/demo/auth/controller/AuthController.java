package com.demo.auth.controller;

import com.demo.auth.generic.ApiResponse;
import com.demo.auth.models.dtos.AuthRequest;
import com.demo.auth.models.dtos.AuthResponse;
import com.demo.auth.models.dtos.RefreshTokenRequest;
import com.demo.auth.models.dtos.RefreshTokenResponse;
import com.demo.auth.models.dtos.SignInRequest;
import com.demo.auth.models.dtos.SignUpRequest;
import com.demo.auth.models.dtos.LogoutRequest;
import com.demo.auth.models.dtos.OAuthLoginRequest;
import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.service.AuthService;
import com.demo.auth.service.LogoutService;
import com.demo.auth.service.TokenRefreshFacade;
import com.demo.auth.service.UserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
public class AuthController {

    private final UserRegistrationService userRegistrationService;
    private final AuthService authService;
    private final TokenRefreshFacade tokenRefreshFacade;
    private final LogoutService logoutService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with local provider")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody SignUpRequest request) {
        ApiResponse<String> response = userRegistrationService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Verify user email", description = "Verifies user email using the 6-digit OTP sent at registration")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @Valid @RequestBody com.demo.auth.models.dtos.VerifyEmailRequest request) {
        ApiResponse<String> response = userRegistrationService.verifyEmail(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Sign in user", description = "Authenticates user and returns access and refresh tokens")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody SignInRequest request) {

        // Map SignInRequest to AuthRequest
        AuthRequest authRequest = new AuthRequest(
                request.usernameOrEmail(),
                request.password(),
                null,
                ProviderType.LOCAL);

        AuthResponse authResponse = authService.login(authRequest);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("User authenticated successfully")
                .data(authResponse)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh tokens", description = "Generates new access and refresh tokens using a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        ApiResponse<RefreshTokenResponse> response = tokenRefreshFacade.refreshTokens(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "OAuth login", description = "Authenticates using an external provider's access token or ID token")
    @PostMapping("/oauth/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse>> oauthLogin(
            @Parameter(description = "Provider name (e.g. google, github)", required = true) @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request) {

        ProviderType providerType;
        try {
            providerType = ProviderType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<AuthResponse>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .success(false)
                            .message("Unsupported OAuth provider: " + provider)
                            .timestamp(Instant.now())
                            .build());
        }

        AuthRequest authRequest = new AuthRequest(
                null,
                null,
                request.token(),
                providerType);

        AuthResponse authResponse = authService.login(authRequest);

        ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("User authenticated successfully via " + provider)
                .data(authResponse)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout user", description = "Revokes refresh token and blacklists current access token")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        ApiResponse<String> response = logoutService.logout(request, authHeader);
        return ResponseEntity.ok(response);
    }
}
