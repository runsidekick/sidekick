package com.runsidekick.broker.service;

import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.WorkspaceSessions;
import com.runsidekick.broker.proxy.listener.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author serkan.ozal
 */
@Service
public interface SessionService {

    String CLIENT_SESSION_KEY_PREFIX = "client";
    String APP_SESSION_KEY_PREFIX = "app";

    static String getAppSessionId(String appInstanceId) {
        return APP_SESSION_KEY_PREFIX + "::" + appInstanceId;
    }

    static String getClientSessionId(String client, String connectionId) {
        return CLIENT_SESSION_KEY_PREFIX + "::" + client + "|" + connectionId;
    }

    Map<String, ChannelInfo> getSessionMap(String workspaceId);

    ChannelInfo getSession(String workspaceId, String sessionId);

    Map<String, ChannelInfo> getSessionGroup(String workspaceId, String sessionGroupId);

    ChannelInfo addSession(ChannelInfo channelInfo, SessionCallback sessionCallback);

    void removeSession(ChannelInfo channelInfo, SessionCallback sessionCallback);

    // Called by tests
    Map<String, WorkspaceSessions> getAllSessionMap();

    void clearSessions();

}
