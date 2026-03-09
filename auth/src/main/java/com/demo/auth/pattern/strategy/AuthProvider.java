package com.demo.auth.pattern.strategy;

import com.demo.auth.models.dtos.AuthRequest;
import com.demo.auth.models.dtos.AuthUser;
import com.demo.auth.models.enums.ProviderType;

public interface AuthProvider{

    ProviderType provider();

    AuthUser authenticate(AuthRequest request);
}
