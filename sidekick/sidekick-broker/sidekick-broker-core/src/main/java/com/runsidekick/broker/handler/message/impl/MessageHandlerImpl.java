package com.runsidekick.broker.handler.message.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runsidekick.broker.error.CodedException;
import com.runsidekick.broker.error.ErrorCodes;
import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.handler.event.EventHandler;
import com.runsidekick.broker.handler.message.MessageHandler;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.app.AppRequestHandler;
import com.runsidekick.broker.handler.request.client.ClientRequestHandler;
import com.runsidekick.broker.handler.request.client.impl.DefaultClientRequestHandler;
import com.runsidekick.broker.model.event.Event;
import com.runsidekick.broker.model.event.EventAck;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;
import com.runsidekick.broker.model.response.impl.ErrorResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.Communicator;
import com.runsidekick.broker.proxy.listener.BrokerListener;
import com.runsidekick.broker.service.ApiAuthenticationService;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author tolgatakir
 */

@Component
public class MessageHandlerImpl implements MessageHandler {
    private static final Logger LOGGER = LogManager.getLogger(MessageHandlerImpl.class);

    private static final String MESSAGE_EVENT_TYPE = "Event";
    private static final String MESSAGE_REQUEST_TYPE = "Request";
    private static final String MESSAGE_CLIENT_KEY = "client";
    private static final String MESSAGE_REQUEST_ID_KEY = "requestId";


    private final Map<String, EventHandler> eventHandlerMap = new HashMap<>();
    private final Map<String, AppRequestHandler> appRequestHandlerMap = new HashMap<>();
    private final Map<String, ClientRequestHandler> clientRequestHandlerMap = new HashMap<>();

    private final ClientRequestHandler defaultClientRequestHandler;

    private final ObjectMapper objectMapper =
            new ObjectMapper().
                    setSerializationInclusion(JsonInclude.Include.NON_NULL).
                    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
                    configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true).
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Communicator communicator;
    private final BrokerListener brokerListener;

    private final ApiAuthenticationService apiAuthenticationService;

    public MessageHandlerImpl(Communicator communicator, List<EventHandler> eventHandlers,
                              List<AppRequestHandler> appRequestHandlers,
                              List<ClientRequestHandler> clientRequestHandlers,
                              Optional<BrokerListener> brokerListener,
                              DefaultClientRequestHandler defaultClientRequestHandler,
                              ApiAuthenticationService apiAuthenticationService) {
        this.communicator = communicator;
        this.brokerListener = brokerListener.orElse(null);
        this.defaultClientRequestHandler = defaultClientRequestHandler;
        for (EventHandler eventHandler : eventHandlers) {
            eventHandlerMap.put(eventHandler.getEventName(), eventHandler);
        }
        for (AppRequestHandler appRequestHandler : appRequestHandlers) {
            appRequestHandlerMap.put(appRequestHandler.getRequestName(), appRequestHandler);
        }
        for (ClientRequestHandler clientRequestHandler : clientRequestHandlers) {
            clientRequestHandlerMap.put(clientRequestHandler.getRequestName(), clientRequestHandler);
        }
        this.apiAuthenticationService = apiAuthenticationService;
    }

    @Override
    public void handleWebSocketMessage(ChannelInfo channelInfo, WebSocketFrame messageFrame) {
        String messageContent = messageFrame.content().toString(Charset.defaultCharset());
        try {
            JSONObject messageObj = new JSONObject(messageContent);
            if (channelInfo.getChannelType() != null) {
                switch (channelInfo.getChannelType()) {
                    case APP:
                        handleAppMessage(channelInfo, messageFrame, messageObj, messageContent);
                        if (brokerListener != null) {
                            brokerListener.onHandleAppMessage(channelInfo, messageObj);
                        }
                        break;
                    case CLIENT:
                        handleClientMessage(channelInfo, messageObj, messageContent);
                        if (brokerListener != null) {
                            brokerListener.onHandleClientMessage(channelInfo, messageObj);
                        }
                        break;
                    case API:
                        handleApiMessage(channelInfo, messageObj, messageContent);
                        if (brokerListener != null) {
                            brokerListener.onHandleApiMessage(channelInfo, messageObj);
                        }
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Error occurred while handling websocket message: %s", messageContent), e);
        }
    }

    private void handleAppMessage(ChannelInfo channelInfo, WebSocketFrame messageFrame,
                                  JSONObject message, String messageRaw) {
        EventContext context = new EventContext();
        try {
            WebSocketFrame newMessageFrame = null;
            String type = null;
            if (message.has("type") && !message.isNull("type")) {
                type = message.getString("type");
            }
            if (MESSAGE_EVENT_TYPE.equalsIgnoreCase(type)) {
                String eventName = null;
                if (message.has("name") && !message.isNull("name")) {
                    eventName = (String) message.get("name");
                }
                if (eventName == null) {
                    LOGGER.error("No event name could be found in client message: {}", messageRaw);
                    return;
                }
                // Check whether there is any event handler
                EventHandler eventHandler = eventHandlerMap.get(eventName);
                if (eventHandler != null) {
                    newMessageFrame = handleWithEventHandler(eventHandler, channelInfo, messageRaw, context);
                }
            } else if (MESSAGE_REQUEST_TYPE.equalsIgnoreCase(type)) {
                String requestName = null;
                if (message.has("name") && !message.isNull("name")) {
                    requestName = (String) message.get("name");
                }
                if (requestName == null) {
                    LOGGER.error("No request name could be found in app request message: {}", messageRaw);
                    return;
                }
                handleWithAppRequestHandler(channelInfo, message, messageRaw, requestName);
            }
            handleAppMessage(channelInfo, messageFrame, message, newMessageFrame, context);
        } catch (Exception exception) {
            LOGGER.error("Error occurred while handling app message: {}", messageRaw, exception);
            handleError(channelInfo, message, exception);
        }
    }

    private WebSocketFrame handleWithEventHandler(EventHandler eventHandler, ChannelInfo channelInfo,
                                                  String messageRaw, EventContext context)
            throws JsonProcessingException {
        WebSocketFrame newMessageFrame = null;
        // If event is not deserialized above, deserialize it here.
        Event event = (Event) objectMapper.readValue(messageRaw, eventHandler.getEventClass());
        context.setRawMessage(messageRaw);
        EventAck eventAck = eventHandler.handleEvent(channelInfo, event, context);
        if (context.isEventUpdated()) {
            String processedEventRaw = objectMapper.writeValueAsString(event);
            // Build new message frame from processed event
            // as the event it might be updated during processing
            newMessageFrame = new TextWebSocketFrame(processedEventRaw);
        }
        if (eventAck == null && event.isSendAck()) {
            eventAck = new EventAck(event.getId());
        }
        if (eventAck != null) {
            String eventAckRaw = objectMapper.writeValueAsString(eventAck);
            channelInfo.getChannel().writeAndFlush(new TextWebSocketFrame(eventAckRaw));
        }
        return newMessageFrame;
    }

    private void handleWithAppRequestHandler(ChannelInfo channelInfo, JSONObject message, String messageRaw,
                                             String requestName) throws JsonProcessingException {
        AppRequestHandler appRequestHandler = appRequestHandlerMap.get(requestName);
        if (appRequestHandler != null) {
            Request request = (Request) objectMapper.readValue(messageRaw, appRequestHandler.getRequestClass());
            RequestContext requestContext = new RequestContext(message);
            Response response = appRequestHandler.handleRequest(channelInfo, request, requestContext);
            String responseRaw = objectMapper.writeValueAsString(response);
            channelInfo.getChannel().writeAndFlush(new TextWebSocketFrame(responseRaw));
        }
    }

    private void handleAppMessage(ChannelInfo channelInfo, WebSocketFrame messageFrame, JSONObject message,
                                  WebSocketFrame newMessageFrame, EventContext context) {
        if (message.has(MESSAGE_CLIENT_KEY) && !message.isNull(MESSAGE_CLIENT_KEY)) {
            String email = message.getString(MESSAGE_CLIENT_KEY);
            if (newMessageFrame != null) {
                communicator.sendMessageToClient(channelInfo, email,
                        newMessageFrame, context);
            } else {
                communicator.sendMessageToClient(channelInfo, email,
                        messageFrame.copy(), context);
            }
        } else if (context.isBroadcast()) {
            if (newMessageFrame != null) {
                communicator.broadcastMessageToClients(channelInfo, newMessageFrame, context);
            } else {
                communicator.broadcastMessageToClients(channelInfo, messageFrame.copy(), context);
            }
        }
    }

    private void handleClientMessage(ChannelInfo channelInfo, JSONObject message, String messageRaw) {
        try {
            String requestName = null;
            if (message.has("name") && !message.isNull("name")) {
                requestName = (String) message.get("name");
            }
            if (requestName == null) {
                LOGGER.error("No request name could be found in client message: {}", messageRaw);
                return;
            }
            handleWithClientRequestHandler(channelInfo, message, messageRaw, requestName);
        } catch (Exception exception) {
            LOGGER.error("Error occurred while handling client message: {}", messageRaw, exception);
            handleError(channelInfo, message, exception);
        }
    }

    private void handleWithClientRequestHandler(ChannelInfo channelInfo, JSONObject message, String messageRaw,
                                                String requestName) throws JsonProcessingException {
        ClientRequestHandler clientRequestHandler =
                clientRequestHandlerMap.getOrDefault(requestName, defaultClientRequestHandler);
        Request request = (Request) objectMapper.readValue(messageRaw, clientRequestHandler.getRequestClass());
        RequestContext requestContext = new RequestContext(message);
        Response response = clientRequestHandler.handleRequest(channelInfo, request, requestContext);
        String responseRaw = objectMapper.writeValueAsString(response);
        channelInfo.getChannel().writeAndFlush(new TextWebSocketFrame(responseRaw));
    }

    private void handleApiMessage(ChannelInfo channelInfo, JSONObject message, String messageRaw) {
        try {
            String requestName = null;
            if (message.has("name") && !message.isNull("name")) {
                requestName = (String) message.get("name");
            }
            if (requestName == null) {
                LOGGER.error("No request name could be found in client message: {}", messageRaw);
                return;
            }
            ChannelInfo clientChannelInfo =
                    apiAuthenticationService
                            .generateClientChannelInfo(channelInfo,
                                    message.getString("client"),
                                    message.getString("workspaceId"));
            handleWithClientRequestHandler(clientChannelInfo, message, messageRaw, requestName);
        } catch (Exception exception) {
            LOGGER.error("Error occurred while handling client message: {}", messageRaw, exception);
            handleError(channelInfo, message, exception);
        }
    }

    private void handleError(ChannelInfo channelInfo, JSONObject message, Exception exception) {
        ErrorResponse response = new ErrorResponse();
        String requestId = null;
        if (message.has(MESSAGE_REQUEST_ID_KEY) && !message.isNull(MESSAGE_REQUEST_ID_KEY)) {
            requestId = message.getString(MESSAGE_REQUEST_ID_KEY);
        }
        response.setRequestId(requestId);
        if (exception instanceof CodedException) {
            CodedException codedException = (CodedException) exception;
            response.setErroneous(true);
            response.setErrorCode(codedException.getCode());
            response.setErrorMessage(codedException.getMessage());
        } else {
            response.setError(ErrorCodes.UNKNOWN);
        }
        try {
            String responseRaw = objectMapper.writeValueAsString(response);
            channelInfo.getChannel().writeAndFlush(new TextWebSocketFrame(responseRaw));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error occurred while sending error response", e);
        }
    }
}
