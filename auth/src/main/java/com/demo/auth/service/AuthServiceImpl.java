package com.demo.auth.service;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.mapper.AuthMapper;
import com.demo.auth.models.dtos.AuthRequest;
import com.demo.auth.models.dtos.AuthResponse;
import com.demo.auth.models.dtos.AuthUser;
import com.demo.auth.models.entities.User;
import com.demo.auth.pattern.factory.AuthProviderFactory;
import com.demo.auth.pattern.strategy.AuthProvider;
import com.demo.auth.repositories.UserRepository;
import com.demo.auth.security.service.JwtService;
import com.demo.auth.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthProviderFactory authProviderFactory;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(AuthRequest request) {

        AuthProvider provider = authProviderFactory.get(request.provider());

        AuthUser user = provider.authenticate(request);

        String accessToken = jwtService.generateToken(user);

        User userEntity = userRepository.findById(user.id())
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND,"User not found"));

        String refreshToken = refreshTokenService.create(userEntity);

        return AuthMapper.toAuthResponse(userEntity, accessToken, refreshToken);
    }
}
