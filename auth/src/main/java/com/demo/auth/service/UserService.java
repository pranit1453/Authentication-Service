package com.demo.auth.service;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.models.dtos.UpdateProfileRequest;
import com.demo.auth.models.dtos.UserProfileResponse;
import com.demo.auth.models.entities.User;
import com.demo.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getMyProfile(String email) {
        User user = getUserByEmail(email);
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);

        log.info("Updating profile for user: {}", email);

        // Update fields securely
        user.setUsername(request.username());

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    @Transactional
    public void deleteMyAccount(String email) {
        User user = getUserByEmail(email);

        log.warn("PERMANENTLY deleting account for user: {}", email);

        userRepository.delete(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "User profile not found"));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getProvider(),
                user.getRoles(),
                user.isEmailVerified(),
                user.getCreatedAt());
    }
}
