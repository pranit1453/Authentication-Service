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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GoogleAuthProvider implements AuthProvider {

    private final UserRepository userRepository;
    private final WebClient webClient;
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={token}";

    @Override
    public ProviderType provider() {
        return ProviderType.GOOGLE;
    }

    @Override
    public AuthUser authenticate(AuthRequest request) {

        if (request.oauthToken() == null || request.oauthToken().isBlank()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Google OAuth token is required");
        }

        // --- Real Google API Token Validation via WebClient ---
        String email;
        String name;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenInfo = webClient.get()
                    .uri(GOOGLE_TOKEN_INFO_URL, request.oauthToken())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (tokenInfo == null || !tokenInfo.containsKey("email")) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid Google token payload");
            }

            email = (String) tokenInfo.get("email");
            name = (String) tokenInfo.getOrDefault("name", "Google User");
        } catch (WebClientResponseException e) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Failed to verify Google Token");
        }

        // Find existing user or create a new one
        User user = userRepository.findByEmail(email).orElseGet(() -> {

            // Generate a random username since we only get an email/name from Google
            String generatedUsername = name.replaceAll("\\s+", "").toLowerCase()
                    + "_" + UUID.randomUUID().toString().substring(0, 5);

            User newUser = User.builder()
                    .email(email)
                    .username(generatedUsername)
                    .password(UUID.randomUUID().toString())
                    .provider(ProviderType.GOOGLE)
                    .enabled(true)
                    .roles(Set.of(RoleType.ROLE_USER))
                    .build();

            return userRepository.save(newUser);
        });

        // Ensure the existing user isn't actually a local user or github user claiming
        // the same email
        if (user.getProvider() != ProviderType.GOOGLE) {
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
