package com.runsidekick.broker.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.service.SessionService;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Component
public class CommunicatorImpl extends Communicator {

    private static final Logger LOGGER = LogManager.getLogger(CommunicatorImpl.class);

    public CommunicatorImpl(SessionService sessionService, ObjectMapper objectMapper) {
        super(sessionService, objectMapper);
    }

    @Override
    public boolean sendMessageToApp(ChannelInfo clientChannelInfo, String appInstanceId, WebSocketFrame message) {
        String workspaceId = clientChannelInfo.getWorkspaceId();
        String sessionId = SessionService.getAppSessionId(appInstanceId);
        ChannelInfo channelInfo = sessionService.getSession(workspaceId, sessionId);
        if (channelInfo == null) {
            LOGGER.error("ChannelInfo is null for given workspaceId {} and appInstanceId {}",
                    workspaceId, appInstanceId);
            message.release();
            return false;
        }
        logDebug("ChannelInfo workspaceId {} and parameter workspaceId {}",
                channelInfo.workspaceId, workspaceId);
        if (workspaceId.equals(channelInfo.workspaceId)) {
            logDebug("ChannelInfo workspaceId and parameter workspaceId is same");
            channelInfo.channel.writeAndFlush(message);
            logDebug("The request has been sent successfully with id {}", appInstanceId);
            return true;
        } else {
            logDebug("ChannelInfo workspaceId and parameter workspaceId is different");
            message.release();
            logDebug("The request could not be sent");
            return false;
        }
    }

    @Override
    public void sendMessageToClient(ChannelInfo clientChannelInfo, String targetClient,
                                    WebSocketFrame message, EventContext context) {
        String workspaceId = clientChannelInfo.getWorkspaceId();
        try {
            Map<String, ChannelInfo> sessionGroup;
            if (context.isBroadcast()) {
                sessionGroup = sessionService.getSessionMap(workspaceId);
            } else {
                sessionGroup = sessionService.getSessionGroup(workspaceId, targetClient);
            }
            if (sessionGroup != null) {
                for (ChannelInfo channelInfo : sessionGroup.values()) {
                    if (workspaceId.equals(channelInfo.workspaceId)
                            && (channelInfo.channelType.equals(ChannelType.CLIENT)
                            || channelInfo.channelType.equals(ChannelType.API))) {
                        channelInfo.channel.writeAndFlush(message.copy());
                    }
                }
            }
        } finally {
            message.release();
        }
    }

    @Override
    public void sendMessageToClients(ChannelInfo appChannelInfo, Object message) {
        String workspaceId = appChannelInfo.getWorkspaceId();
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Throwable t) {
            LOGGER.error(
                    String.format(
                            "Unable to serialize message to send clients in user workspace %s", workspaceId),
                    t);
            return;
        }
        Map<String, ChannelInfo> sessionMap = sessionService.getSessionMap(workspaceId);
        if (sessionMap != null) {
            for (ChannelInfo ci : sessionMap.values()) {
                if (ci.channelType.equals(ChannelType.CLIENT)
                || ci.channelType.equals(ChannelType.API)) {
                    ClientMetadata clientMetadata = (ClientMetadata) ci.getChannelMetadata();
                    try {
                        ci.channel.writeAndFlush(new TextWebSocketFrame(messageJson));
                    } catch (Throwable t) {
                        LOGGER.error(
                                String.format(
                                        "Unable to send message to client with id=%s and email=%s",
                                        clientMetadata.userId, clientMetadata.email),
                                t);
                    }
                }
            }
        }
    }

    @Override
    public void sendRequestToApps(ChannelInfo clientChannelInfo, Object message, List<String> applications) {
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Throwable t) {
            LOGGER.error(
                    String.format(
                            "Unable to serialize message to send clients in workspace %s",
                            clientChannelInfo.getWorkspaceId()),
                    t);
            return;
        }
        try {
            for (String appInstanceId : applications) {
                logDebug("Send request to application with id {}", appInstanceId);
                boolean response = sendMessageToApp(
                        clientChannelInfo, appInstanceId, new TextWebSocketFrame(messageJson));
                logDebug("Response of request to application is {}", response);
            }
        } catch (Exception e) {
            LOGGER.error("Unable to send request to applications. Exception is ", e);
        }
    }
}
