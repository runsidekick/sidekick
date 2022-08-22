package com.runsidekick.broker.proxy;

/**
 * @author serkan.ozal
 */
public class ClientMetadata implements ChannelMetadata {

    final String userId;
    final String email;

    public ClientMetadata(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

}
