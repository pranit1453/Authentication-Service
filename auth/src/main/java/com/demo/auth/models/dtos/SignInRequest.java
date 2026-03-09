package com.demo.auth.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
        @NotBlank(message = "Username or email required")
        String usernameOrEmail,

        @NotBlank(message = "Password required")
        String password
) {}
