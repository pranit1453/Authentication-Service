package com.demo.auth.service;

import com.demo.auth.models.dtos.AuthRequest;
import com.demo.auth.models.dtos.AuthResponse;

public interface AuthService {

    public AuthResponse login(AuthRequest request);
}
