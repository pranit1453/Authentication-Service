package com.demo.auth.generic;

import lombok.Builder;

import java.time.Instant;
@Builder
public record ApiResponse<T>(
        int status,
        boolean success,
        String message,
        T data,
        String failure_message,
        Instant timestamp
) {}
