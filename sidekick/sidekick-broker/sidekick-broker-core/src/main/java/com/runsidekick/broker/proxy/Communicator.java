package com.runsidekick.broker.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.service.SessionService;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author serkan.ozal
 */
@Component
public abstract class Communicator {

    private static final Logger LOGGER = LogManager.getLogger(Communicator.class);

    protected final SessionService sessionService;

    protected final ObjectMapper objectMapper;

    public Communicator(SessionService sessionService, ObjectMapper objectMapper) {
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    public abstract boolean sendMessageToApp(
            ChannelInfo clientChannelInfo, String appInstanceId, WebSocketFrame message);

    public void broadcastMessageToClients(ChannelInfo clientChannelInfo, WebSocketFrame message,
                                          EventContext eventContext) {
        sendMessageToClient(clientChannelInfo, null, message, eventContext);
    }

    public abstract void sendMessageToClient(ChannelInfo clientChannelInfo, String targetClient, WebSocketFrame message,
                                    EventContext eventContext);

    public abstract void sendMessageToClients(ChannelInfo appChannelInfo, Object message);

    public abstract void sendRequestToApps(ChannelInfo clientChannelInfo, Object message, List<String> applications);

    protected void logDebug(String message, String... keys) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(message, keys);
        }
    }

    protected void logDebug(String message, boolean key) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(message, key);
        }
    }

}
