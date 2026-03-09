package com.demo.auth.provider;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.models.dtos.AuthRequest;
import com.demo.auth.models.dtos.AuthUser;
import com.demo.auth.models.entities.User;
import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.models.enums.RoleType;
import com.demo.auth.pattern.strategy.AuthProvider;
import com.demo.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GithubAuthProvider implements AuthProvider {

    private final UserRepository userRepository;
    private final WebClient webClient;
    private static final String GITHUB_USER_API_URL = "https://api.github.com/user";
    private static final String GITHUB_EMAILS_API_URL = "https://api.github.com/user/emails";

    @Override
    public ProviderType provider() {
        return ProviderType.GITHUB;
    }

    @Override
    public AuthUser authenticate(AuthRequest request) {

        if (request.oauthToken() == null || request.oauthToken().isBlank()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "GitHub OAuth access token is required");
        }

        // --- Real GitHub API Token Validation via WebClient ---
        String email = null;
        String baseUsername;

        try {
            // 1. Fetch User Profile
            @SuppressWarnings("unchecked")
            Map<String, Object> userProfile = webClient.get()
                    .uri(GITHUB_USER_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + request.oauthToken())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (userProfile == null) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid GitHub token payload");
            }

            baseUsername = (String) userProfile.getOrDefault("login", "github_user");
            email = (String) userProfile.get("email");

            // 2. Fetch User Emails if the main profile email is null/private
            if (email == null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> emailsData = webClient.get()
                        .uri(GITHUB_EMAILS_API_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + request.oauthToken())
                        .retrieve()
                        .bodyToMono(List.class)
                        .block();

                if (emailsData != null) {
                    for (Map<String, Object> emailObj : emailsData) {
                        Boolean primary = (Boolean) emailObj.get("primary");
                        if (Boolean.TRUE.equals(primary)) {
                            email = (String) emailObj.get("email");
                            break;
                        }
                    }
                }
            }

            if (email == null) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "GitHub account does not have a primary email");
            }

        } catch (WebClientResponseException e) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Failed to verify GitHub Token");
        }

        String username = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5);

        String finalEmail = email;
        // Find existing user or create a new one
        User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {

            User newUser = User.builder()
                    .email(finalEmail)
                    .username(username)
                    .password(UUID.randomUUID().toString())
                    .provider(ProviderType.GITHUB)
                    .enabled(true)
                    .roles(Set.of(RoleType.ROLE_USER))
                    .build();

            return userRepository.save(newUser);
        });

        // Prevent provider mismatch attacks
        if (user.getProvider() != ProviderType.GITHUB) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Email already exists with a different provider");
        }

        if (!user.isEnabled()) {
            throw new AuthException(HttpStatus.FORBIDDEN, "User account is disabled");
        }

        return new AuthUser(
                user.getUserId(),
                user.getEmail(),
                user.getRoles());
    }
}
