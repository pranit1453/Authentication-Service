package com.demo.auth.controller;

import com.demo.auth.generic.ApiResponse;
import com.demo.auth.models.dtos.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.demo.auth.models.dtos.LogoutRequest;
import com.demo.auth.models.dtos.UpdateProfileRequest;
import com.demo.auth.models.dtos.UserProfileResponse;
import com.demo.auth.service.LogoutService;
import com.demo.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.Instant;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints restricted to USER role")
public class UserController {

    private final LogoutService logoutService;
    private final UserService userService;

    @Operation(summary = "Get current user profile", description = "Returns the profile information of the currently authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        UserProfileResponse profileData = userService.getMyProfile(authUser.email());

        ApiResponse<UserProfileResponse> response = ApiResponse.<UserProfileResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("User profile retrieved successfully")
                .data(profileData)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update my profile", description = "Updates the username of the currently authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        UserProfileResponse updatedProfile = userService.updateMyProfile(authUser.email(), request);

        ApiResponse<UserProfileResponse> response = ApiResponse.<UserProfileResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Your profile has been updated successfully.")
                .data(updatedProfile)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete my account", description = "Permanently deletes the currently authenticated user's account.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyAccount() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        userService.deleteMyAccount(authUser.email());

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("We're sorry to see you go! Your account has been permanently deleted.")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User Dashboard", description = "A simple welcome dashboard endpoint", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Welcome User!")
                .data("Hello " + authUser.email() + ", welcome to your User Dashboard.")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout User", description = "Revokes refresh token and blacklists current access token for User context", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        ApiResponse<String> response = logoutService.logout(request, authHeader);
        return ResponseEntity.ok(response);
    }
}
