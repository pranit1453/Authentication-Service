package com.demo.auth.controller;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.generic.ApiResponse;
import com.demo.auth.models.dtos.SetupAdminRequest;
import com.demo.auth.models.entities.User;
import com.demo.auth.models.enums.RoleType;
import com.demo.auth.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Setup", description = "Endpoints for manual secure admin provisioning")
public class AdminSetupController {

    private final UserRepository userRepository;

    @Value("${api.setup.admin.secret}")
    private String adminSetupSecret;

    @Operation(summary = "Grant Admin Role", description = "Promote a registered user to ADMIN. Requires a super secret key header.")
    @PostMapping("/setup-admin")
    public ResponseEntity<ApiResponse<String>> setupAdmin(
            @RequestHeader("X-Setup-Secret") String secret,
            @Valid @RequestBody SetupAdminRequest request) {

        log.info("Received request to promote {} to Admin", request.email());

        // Validate secret
        if (!adminSetupSecret.equals(secret)) {
            log.warn("Failed Admin Setup attempt: Invalid Secret Key provided.");
            throw new AuthException(HttpStatus.FORBIDDEN, "Invalid setup secret key");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND,
                        "User with email not found. They must register first."));

        // Check if already admin
        if (user.getRoles().contains(RoleType.ROLE_ADMIN)) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "User is already an Administrator");
        }

        user.getRoles().add(RoleType.ROLE_ADMIN);
        userRepository.save(user);

        log.info("Successfully granted ROLE_ADMIN to {}", user.getEmail());

        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Successfully granted Administrator privileges to user")
                .data(user.getEmail())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
