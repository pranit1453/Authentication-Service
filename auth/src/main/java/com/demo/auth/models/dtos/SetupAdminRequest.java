package com.demo.auth.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SetupAdminRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
}
