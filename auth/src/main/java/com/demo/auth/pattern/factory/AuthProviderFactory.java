package com.demo.auth.pattern.factory;

import com.demo.auth.exception.custom.AuthException;
import com.demo.auth.models.enums.ProviderType;
import com.demo.auth.pattern.strategy.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthProviderFactory {

    private final Map<ProviderType, AuthProvider> providers;

    public AuthProviderFactory(List<AuthProvider> list){
        this.providers = new HashMap<>();

        for(AuthProvider provider : list)
            providers.put(provider.provider(),provider);
    }

    public AuthProvider get(ProviderType type){
        AuthProvider provider = providers.get(type);

        if(provider == null)
            throw new AuthException(HttpStatus.BAD_REQUEST,"Unsupported provider");

        return provider;
    }
}
