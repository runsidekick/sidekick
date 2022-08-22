package com.runsidekick.broker.service;

import com.runsidekick.broker.proxy.ChannelInfo;
import org.springframework.stereotype.Service;

@Service
public abstract class ApiAuthenticationService extends AuthenticationService {

    private final SessionService sessionService;

    protected ApiAuthenticationService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public abstract ChannelInfo generateClientChannelInfo(ChannelInfo channelInfo, String email, String workspaceId)
            throws Exception;

    protected SessionService getSessionService() {
        return sessionService;
    }
}
