package com.runsidekick.broker.handler.request.client.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.runsidekick.audit.logger.dto.AuditLog;
import com.runsidekick.broker.handler.request.client.ClientRequestHandler;
import com.runsidekick.broker.model.request.Request;
import com.runsidekick.broker.model.response.Response;
import com.runsidekick.broker.model.response.impl.TargetApplicationNotAvailableResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.proxy.Communicator;
import com.runsidekick.broker.service.ApplicationService;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author serkan.ozal
 */
public abstract class BaseClientRequestHandler<Req extends Request, Res extends Response>
        implements ClientRequestHandler<Req, Res> {

    protected final Logger logger = LogManager.getLogger(getClass());

    protected final String requestName;
    protected final Class<Req> requestClass;
    protected final Class<Res> responseClass;

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    protected Communicator communicator;

    protected final ObjectMapper objectMapper =
            new ObjectMapper().
                    setSerializationInclusion(JsonInclude.Include.NON_NULL).
                    configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
                    configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true).
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    protected BaseClientRequestHandler(String requestName, Class<Req> requestClass, Class<Res> responseClass) {
        this.requestName = requestName;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    @Override
    public String getRequestName() {
        return requestName;
    }

    @Override
    public Class<Req> getRequestClass() {
        return requestClass;
    }

    @Override
    public Class<Res> getResponseClass() {
        return responseClass;
    }

    protected void sendRequestToApps(ChannelInfo channelInfo, String requestId, JSONObject message,
                                   List<String> targetApplicationIds) {
        if (targetApplicationIds != null) {
            message.put("client", ((ClientMetadata) channelInfo.getChannelMetadata()).getEmail());
            for (String appInstanceId : targetApplicationIds) {
                sendMessageSingleApplication(channelInfo, requestId, message, appInstanceId);
            }
        }
        channelInfo.getChannel().flush();
    }

    private void sendMessageSingleApplication(ChannelInfo channelInfo, String requestId, JSONObject message,
                                              String appInstanceId) {
        String messageString = message.toString();
        try {
            sendRequestToApp(channelInfo, requestId, messageString, appInstanceId);
        } catch (Exception e) {
            logger.error("Unable to send request to applications", e);
        }
    }


    private void sendRequestToApp(ChannelInfo channelInfo, String requestId, String messageString,
                                  String appInstanceId) throws JsonProcessingException {
        TextWebSocketFrame messageFrame = new TextWebSocketFrame(messageString);
        boolean sent = communicator.sendMessageToApp(channelInfo, appInstanceId, messageFrame);
        if (!sent) {
            Response response = new TargetApplicationNotAvailableResponse(requestId, appInstanceId);
            String responseRaw = objectMapper.writeValueAsString(response);
            channelInfo.getChannel().write(new TextWebSocketFrame(responseRaw));
        }
    }

    protected void setAuditLogUserInfo(AuditLog auditLog, ChannelInfo channelInfo, String client) {
        auditLog.setIp(channelInfo.getIp());
        auditLog.setAccountId(channelInfo.getAccountId());
        auditLog.setEmail(client);
        auditLog.setUserId(channelInfo.getUserId());
    }
}
