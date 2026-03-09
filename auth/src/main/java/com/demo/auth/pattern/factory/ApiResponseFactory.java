package com.demo.auth.pattern.factory;

import com.demo.auth.generic.ApiResponse;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ApiResponseFactory <T>{

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {

        return ApiResponse.<T>builder()
                .status(status.value())
                .success(true)
                .message(message)
                .data(data)
                .failure_message(null)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> failure(HttpStatus status, String failureMessage) {

        return ApiResponse.<T>builder()
                .status(status.value())
                .success(false)
                .message(null)
                .data(null)
                .failure_message(failureMessage)
                .timestamp(Instant.now())
                .build();
    }}
