package com.demo.auth.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
        @NotBlank(message = "Token is required") String token) {
}
