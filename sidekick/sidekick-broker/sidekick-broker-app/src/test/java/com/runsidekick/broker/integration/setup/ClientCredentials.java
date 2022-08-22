package com.runsidekick.broker.integration.setup;

/**
 * @author serkan.ozal
 */
public class ClientCredentials {

    private final String token;
    private final boolean authenticateOverPath;
    private final boolean authenticateWithToken;

    public ClientCredentials(String token) {
        this.token = token;
        this.authenticateOverPath = false;
        this.authenticateWithToken = true;
    }

    public boolean isAuthenticateOverPath() {
        return authenticateOverPath;
    }

    public String getToken() {
        return token;
    }

    public boolean isAuthenticateWithToken() {
        return authenticateWithToken;
    }
}
