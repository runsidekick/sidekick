package com.runsidekick.api.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private String apiKey;

    public ApiKeyAuthentication(String apiKey) {
        super(null);
        this.apiKey = apiKey;
        setAuthenticated(false);
    }

    @Override
    public Object getPrincipal() {
        return this.apiKey;
    }

    @Override
    public Object getCredentials() {
        return this.apiKey;
    }

    public String getEmail() {
        return this.apiKey;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        apiKey = null;
    }

}