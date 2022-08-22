package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.SessionService;
import com.runsidekick.broker.proxy.WorkspaceSessions;
import com.runsidekick.broker.proxy.listener.SessionCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author serkan.ozal
 */
@Component
public class SessionServiceImpl implements SessionService {

    private final Logger logger = LogManager.getLogger(getClass());

    private final Map<String, WorkspaceSessions> workspaceSessionMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, WorkspaceSessions> getAllSessionMap() {
        return Collections.unmodifiableMap(workspaceSessionMap);
    }

    public Map<String, ChannelInfo> getSessionMap(String workspaceId) {
        WorkspaceSessions workspaceSessions = workspaceSessionMap.get(workspaceId);
        if (workspaceSessions == null) {
            return null;
        }
        return Collections.unmodifiableMap(workspaceSessions.getSessionMap());
    }

    public ChannelInfo getSession(String workspaceId, String sessionId) {
        WorkspaceSessions workspaceSessions = workspaceSessionMap.get(workspaceId);
        if (workspaceSessions != null && sessionId != null) {
            return workspaceSessions.getSession(sessionId);
        }
        return null;
    }

    public Map<String, ChannelInfo> getSessionGroup(String workspaceId, String sessionGroupId) {
        WorkspaceSessions workspaceSessions = workspaceSessionMap.get(workspaceId);
        if (workspaceSessions != null && sessionGroupId != null) {
            return workspaceSessions.getSessionGroup(sessionGroupId);
        }
        return null;
    }

    public ChannelInfo addSession(ChannelInfo channelInfo, SessionCallback sessionCallback) {
        String workspaceId = channelInfo.getWorkspaceId();
        String sessionId = channelInfo.getSessionId();
        if (workspaceId != null && sessionId != null) {
            synchronized (workspaceSessionMap) {
                WorkspaceSessions workspaceSessions = workspaceSessionMap.get(workspaceId);
                if (workspaceSessions == null) {
                    workspaceSessions = new WorkspaceSessions();
                    workspaceSessionMap.put(workspaceId, workspaceSessions);
                }
                ChannelInfo existingChannelInfo = workspaceSessions.putSession(sessionId, channelInfo);
                switch (channelInfo.getChannelType()) {
                    case APP:
                        if (sessionCallback != null) {
                            sessionCallback.onApplicationSessionAdd(channelInfo);
                        }
                        break;
                    case CLIENT:
                        if (sessionCallback != null) {
                            sessionCallback.onClientSessionAdd(channelInfo);
                        }
                        break;
                    case API:
                        if (sessionCallback != null) {
                            sessionCallback.onApiSessionAdd(channelInfo);
                        }
                        break;
                    default:
                        break;
                }
                return existingChannelInfo;
            }
        }
        return null;
    }

    public void removeSession(ChannelInfo channelInfo, SessionCallback sessionCallback) {
        String workspaceId = channelInfo.getWorkspaceId();
        String sessionId = channelInfo.getSessionId();
        if (workspaceId != null && sessionId != null) {
            synchronized (workspaceSessionMap) {
                WorkspaceSessions workspaceSessions = workspaceSessionMap.get(workspaceId);
                if (workspaceSessions != null && sessionId != null) {
                    boolean removed = workspaceSessions.removeSession(sessionId, channelInfo);
                    if (removed) {
                        if (workspaceSessions.sessionCount() == 0) {
                            workspaceSessionMap.remove(workspaceId);
                        }
                        switch (channelInfo.getChannelType()) {
                            case APP:
                                if (sessionCallback != null) {
                                    sessionCallback.onApplicationSessionRemove(channelInfo);
                                }
                                break;
                            case CLIENT:
                                if (sessionCallback != null) {
                                    sessionCallback.onClientSessionRemove(channelInfo);
                                }
                                break;
                            case API:
                                if (sessionCallback != null) {
                                    sessionCallback.onApiSessionRemove(channelInfo);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    // Called by tests
    public void clearSessions() {
        Iterator<WorkspaceSessions> iter1 = workspaceSessionMap.values().iterator();
        while (iter1.hasNext()) {
            WorkspaceSessions sourceSessionMap = iter1.next();
            Iterator<ChannelInfo> iter2 = sourceSessionMap.getSessionMap().values().iterator();
            while (iter2.hasNext()) {
                ChannelInfo channelInfo = iter2.next();
                try {
                    channelInfo.getChannel().close();
                } catch (Throwable t) {
                    logger.error("Unable to close session", t);
                }
                iter2.remove();
            }
            iter1.remove();
        }
    }

}
