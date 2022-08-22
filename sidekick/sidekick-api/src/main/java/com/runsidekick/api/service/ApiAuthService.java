package com.runsidekick.api.service;

import com.runsidekick.api.security.ApiKeyAuthentication;

/**
 * @author yasin.kalafat
 */
public interface ApiAuthService {
    ApiKeyAuthentication getCurrentUser();

    String getApiToken();
}
