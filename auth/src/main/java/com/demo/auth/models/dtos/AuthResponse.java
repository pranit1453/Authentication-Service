package com.demo.auth.models.dtos;

import com.demo.auth.models.enums.RoleType;

import java.util.Set;

public record AuthResponse(
        String accessToken,

        String refreshToken,

        String tokenType,

        Long userId,

        String username,

        String email,

        Set<RoleType> roles
) {}
