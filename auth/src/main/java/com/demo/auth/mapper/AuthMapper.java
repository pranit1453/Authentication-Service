package com.demo.auth.mapper;

import com.demo.auth.models.dtos.AuthResponse;
import com.demo.auth.models.dtos.AuthUser;
import com.demo.auth.models.dtos.RefreshTokenResponse;
import com.demo.auth.models.dtos.SignUpRequest;
import com.demo.auth.models.entities.User;
import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.models.enums.RoleType;

import java.util.Set;

public final class AuthMapper {

    private AuthMapper(){}

    public static AuthUser toAuthUser(User user){
        return new AuthUser(
                user.getUserId(),
                user.getEmail(),
                user.getRoles()
        );
    }

    public static AuthResponse toAuthResponse(User user, String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }

    public static User toUser(SignUpRequest request, String encodedPassword) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .provider(ProviderType.LOCAL)
                .enabled(true)
                .roles(Set.of(RoleType.ROLE_USER))
                .build();
    }

    public static RefreshTokenResponse toRefreshTokenResponse(String accessToken,String refreshToken) {        return new RefreshTokenResponse(
                accessToken,
                refreshToken,
                "Bearer"
        );
    }
}
