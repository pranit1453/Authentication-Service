package com.demo.auth.models.dtos;

import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.models.enums.RoleType;

import java.time.Instant;
import java.util.Set;

public record UserProfileResponse(
        Long userId,
        String username,
        String email,
        ProviderType provider,
        Set<RoleType> roles,
        boolean emailVerified,
        Instant createdAt) {
}
