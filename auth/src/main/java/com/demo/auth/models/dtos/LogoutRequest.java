package com.demo.auth.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "Refresh token is required") String refreshToken) {
}
