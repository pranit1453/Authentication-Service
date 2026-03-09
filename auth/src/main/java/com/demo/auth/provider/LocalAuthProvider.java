package com.demo.auth.provider;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.mapper.AuthMapper;
import com.demo.auth.models.dtos.AuthRequest;
import com.demo.auth.models.dtos.AuthUser;
import com.demo.auth.models.entities.User;
import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.pattern.strategy.AuthProvider;
import com.demo.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalAuthProvider implements AuthProvider {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ProviderType provider() {
        return ProviderType.LOCAL;
    }

    @Override
    public AuthUser authenticate(AuthRequest request) {

        // Validate request
        if (request.email() == null || request.email().isBlank() ||
                request.password() == null || request.password().isBlank()) {

            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required");
        }

        // Find user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        // Validate provider
        if (user.getProvider() != ProviderType.LOCAL) {
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid authentication provider");
        }

        // Check account enabled
        if (!user.isEnabled()) {
            throw new AuthException(
                    HttpStatus.FORBIDDEN,
                    "User account is disabled");
        }

        // Check if account locked
        if (user.isLocked()) {
            userRepository.save(user); // persist auto unlock if triggered
            throw new AuthException(
                    HttpStatus.FORBIDDEN,
                    "Account locked due to multiple failed login attempts");
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new AuthException(
                    HttpStatus.FORBIDDEN,
                    "Please verify your email address before signing in");
        }

        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {

            user.increaseFailedAttempts();

            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount();
            }

            userRepository.save(user);

            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        // Successful login → reset attempts
        user.resetFailedAttempts();
        userRepository.save(user);

        return AuthMapper.toAuthUser(user);
    }
}
