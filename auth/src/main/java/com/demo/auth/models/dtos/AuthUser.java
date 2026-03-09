package com.demo.auth.models.dtos;

import com.demo.auth.models.enums.RoleType;

import java.util.Set;

public record AuthUser(
        Long id,
        String email,
        Set<RoleType> roles
) {}
