package com.demo.auth.pattern.factory;

import com.demo.auth.oauth.GithubOAuthUserInfo;
import com.demo.auth.oauth.GoogleOAuthUserInfo;
import com.demo.auth.pattern.strategy.OAuthUserInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuthUserInfoFactory {

    public OAuthUserInfo getUserInfo(String provider, Map<String, Object> attributes) {

        return switch (provider.toLowerCase()) {

            case "google" -> new GoogleOAuthUserInfo(attributes);

            case "github" -> new GithubOAuthUserInfo(attributes);

            default -> throw new RuntimeException("Unsupported provider: " + provider);
        };
    }
}
