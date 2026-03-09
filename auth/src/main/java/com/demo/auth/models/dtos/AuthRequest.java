package com.demo.auth.models.dtos;

import com.demo.auth.models.enums.ProviderType;
import jakarta.validation.constraints.Email;

public record AuthRequest(
        @Email
        String email,

        String password,

        String oauthToken,

        ProviderType provider
) {}
