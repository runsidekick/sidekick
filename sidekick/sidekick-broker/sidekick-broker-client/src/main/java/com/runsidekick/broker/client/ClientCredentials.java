package com.runsidekick.broker.client;
/**
 * @author yasin.kalafat
 */
public class ClientCredentials {

    private final String email;
    private final String token;

    public ClientCredentials(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

}