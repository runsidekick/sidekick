package com.runsidekick.broker.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author serkan.ozal
 */
public class WorkspaceSessions {

    final Map<String, ChannelInfo> sessionMap = new ConcurrentHashMap<>();
    final Map<String, Map<String, ChannelInfo>> sessionGroupMap = new ConcurrentHashMap<>();

    public ChannelInfo getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public Map<String, ChannelInfo> getSessionGroup(String sessionGroupId) {
        return sessionGroupMap.get(sessionGroupId);
    }

    public ChannelInfo putSession(String sessionId, ChannelInfo channelInfo) {
        ChannelInfo existingSessionInfo = sessionMap.put(sessionId, channelInfo);
        String sessionGroupId = channelInfo.sessionGroupId;
        if (sessionGroupId != null) {
            Map<String, ChannelInfo> sessionGroup = sessionGroupMap.get(sessionGroupId);
            if (sessionGroup == null) {
                sessionGroup = new ConcurrentHashMap<>();
                sessionGroupMap.put(sessionGroupId, sessionGroup);
            }
            sessionGroup.put(sessionId, channelInfo);
        }
        return existingSessionInfo;
    }

    public boolean removeSession(String sessionId, ChannelInfo channelInfo) {
        boolean removed = sessionMap.remove(sessionId, channelInfo);
        String sessionGroupId = channelInfo.sessionGroupId;
        if (sessionGroupId != null) {
            Map<String, ChannelInfo> sessionGroup = sessionGroupMap.get(sessionGroupId);
            if (sessionGroup != null) {
                sessionGroup.remove(sessionId);
            }
        }
        return removed;
    }

    public int sessionCount() {
        return sessionMap.size();
    }

    public Map<String, ChannelInfo> getSessionMap() {
        return sessionMap;
    }
}
