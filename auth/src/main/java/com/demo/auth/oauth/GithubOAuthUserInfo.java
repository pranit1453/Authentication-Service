package com.demo.auth.oauth;

import com.demo.auth.pattern.strategy.OAuthUserInfo;

import java.util.Map;

public class GithubOAuthUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public GithubOAuthUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}