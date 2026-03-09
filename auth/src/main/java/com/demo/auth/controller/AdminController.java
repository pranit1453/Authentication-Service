package com.demo.auth.controller;

import com.demo.auth.generic.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.demo.auth.models.dtos.LogoutRequest;
import com.demo.auth.models.dtos.UserDetailsResponse;
import com.demo.auth.service.AdminUserService;
import com.demo.auth.service.LogoutService;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints restricted to ADMIN role")
public class AdminController {

    private final AdminUserService adminUserService;
    private final LogoutService logoutService;

    @Operation(summary = "Admin Hello", description = "A simple endpoint accessible only to administrators", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<String>> sayHello() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Hello Administrator!")
                .data("You have successfully accessed a secured admin endpoint.")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users", description = "Returns a paginated list of users with filtering and sorting options", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDetailsResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean locked) {

        Page<UserDetailsResponse> usersData = adminUserService.getUsers(page, size, sortBy, sortDir, search, enabled,
                locked);

        ApiResponse<Page<UserDetailsResponse>> response = ApiResponse.<Page<UserDetailsResponse>>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Users retrieved successfully")
                .data(usersData)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout Admin", description = "Revokes refresh token and blacklists current access token for Admin context", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @Valid @RequestBody LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        ApiResponse<String> response = logoutService.logout(request, authHeader);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lock User Account", description = "Manually locks a user's account and instantly sends them an email notification.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/users/{userId}/lock")
    public ResponseEntity<ApiResponse<String>> lockUser(@PathVariable Long userId) {
        adminUserService.lockUser(userId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("User account safely locked.")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Unlock User Account", description = "Manually unlocks a user's account and instantly sends them an email notification.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/users/{userId}/unlock")
    public ResponseEntity<ApiResponse<String>> unlockUser(@PathVariable Long userId) {
        adminUserService.unlockUser(userId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("User account successfully unlocked.")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(response);
    }
}
