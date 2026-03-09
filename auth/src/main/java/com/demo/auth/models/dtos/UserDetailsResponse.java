package com.demo.auth.models.dtos;

import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.models.enums.RoleType;

import java.util.Set;

public record UserDetailsResponse(
        Long userId,
        String username,
        String email,
        boolean enabled,
        boolean accountLocked,
        int failedAttempts,
        Set<RoleType> roles,
        ProviderType provider) {
}
