package com.demo.auth.service;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.generic.ApiResponse;
import com.demo.auth.mapper.AuthMapper;
import com.demo.auth.models.dtos.SignUpRequest;
import com.demo.auth.models.entities.User;
import com.demo.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public ApiResponse<String> registerUser(SignUpRequest request) {

        // We cannot modify UserRepository to add existsByEmail, but it has findByEmail
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AuthException(HttpStatus.CONFLICT, "Email is already registered");
        }

        try {
            User user = AuthMapper.toUser(request, passwordEncoder.encode(request.password()));
            userRepository.save(user);

            // Send OTP Asynchronously
            emailVerificationService.generateAndSendOtp(user.getEmail());

            return ApiResponse.<String>builder()
                    .status(HttpStatus.CREATED.value())
                    .success(true)
                    .message("User registered successfully. Please check your email for the OTP.")
                    .data(user.getEmail())
                    .timestamp(Instant.now())
                    .build();

        } catch (DataIntegrityViolationException e) {
            // Fallback for username uniqueness if DB has a constraint, since we can't add
            // findByUsername to UserRepository
            throw new AuthException(HttpStatus.CONFLICT, "Username or Email is already taken");
        } catch (Exception e) {
            throw new AuthException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred during registration");
        }
    }

    public ApiResponse<String> verifyEmail(com.demo.auth.models.dtos.VerifyEmailRequest request) {
        boolean isValid = emailVerificationService.verifyOtp(request.email(), request.otp());
        if (!isValid) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        return ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Email verified successfully")
                .timestamp(Instant.now())
                .build();
    }
}
