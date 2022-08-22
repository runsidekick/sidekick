package com.runsidekick.api.service.impl;

import com.runsidekick.api.security.ApiKeyAuthentication;
import com.runsidekick.api.service.ApiAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author yasin.kalafat
 */
@Service
public class ApiAuthServiceImpl implements ApiAuthService {

    @Value("${api.token:}")
    private String apiToken;

    @Override
    public ApiKeyAuthentication getCurrentUser() {
        ApiKeyAuthentication auth = (ApiKeyAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() == null) {
            throw new RuntimeException("User not found");
        }
        return auth;
    }

    @Override
    public String getApiToken() {
        return apiToken;
    }

}
