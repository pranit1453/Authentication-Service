package com.demo.auth.security.oauth2;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.models.dtos.AuthUser;
import com.demo.auth.models.entities.User;
import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.models.enums.RoleType;
import com.demo.auth.pattern.factory.OAuthUserInfoFactory;
import com.demo.auth.pattern.strategy.OAuthUserInfo;
import com.demo.auth.repositories.UserRepository;
import com.demo.auth.security.service.JwtService;
import com.demo.auth.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final OAuthUserInfoFactory oAuthUserInfoFactory;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        OAuth2User oauthUser = authToken.getPrincipal();

        if (oauthUser == null) {
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "OAuth authentication failed: user information not found");
        }

        String providerId = authToken.getAuthorizedClientRegistrationId();

        ProviderType provider = ProviderType.valueOf(providerId.toUpperCase());

        OAuthUserInfo userInfo =
                oAuthUserInfoFactory.getUserInfo(providerId, oauthUser.getAttributes());

        String email = userInfo.getEmail();
        String name = userInfo.getName();

        if (email == null || email.isBlank()) {
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "OAuth provider did not return email");
        }

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {

                    if (existingUser.getProvider() != provider) {
                        throw new AuthException(
                                HttpStatus.BAD_REQUEST,
                                "Account already registered using " + existingUser.getProvider());
                    }

                    return existingUser;
                })
                .orElseGet(() -> {

                    String safeName = (name == null || name.isBlank()) ? "user" : name;

                    String username = safeName.replaceAll("\\s+", "")
                            .toLowerCase()
                            + "_" + UUID.randomUUID().toString().substring(0,5);

                    User newUser = User.builder()
                            .email(email)
                            .username(username)
                            .password(UUID.randomUUID().toString())
                            .provider(provider)
                            .enabled(true)
                            .roles(Set.of(RoleType.ROLE_USER))
                            .build();

                    return userRepository.save(newUser);
                });

        AuthUser authUser = new AuthUser(
                user.getUserId(),
                user.getEmail(),
                user.getRoles());

        String accessToken = jwtService.generateToken(authUser);
        String refreshToken = refreshTokenService.create(user);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
        {
            "accessToken":"%s",
            "refreshToken":"%s"
        }
        """.formatted(accessToken, refreshToken));
    }
}