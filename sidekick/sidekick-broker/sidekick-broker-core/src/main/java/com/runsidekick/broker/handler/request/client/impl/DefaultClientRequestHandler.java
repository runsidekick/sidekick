package com.runsidekick.broker.handler.request.client.impl;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.DefaultApplicationAwareRequest;
import com.runsidekick.broker.model.response.impl.DefaultApplicationAwareResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author tolgatakir
 */
@Component
public class DefaultClientRequestHandler extends BaseClientRequestHandler<DefaultApplicationAwareRequest,
        DefaultApplicationAwareResponse> {

    public static final String REQUEST_NAME = "DefaultApplicationAwareRequest";

    DefaultClientRequestHandler() {
        super(REQUEST_NAME, DefaultApplicationAwareRequest.class, DefaultApplicationAwareResponse.class);
    }

    @SneakyThrows
    @Override
    public DefaultApplicationAwareResponse handleRequest(ChannelInfo channelInfo,
                                                         DefaultApplicationAwareRequest request,
                                                         RequestContext requestContext) {
        DefaultApplicationAwareResponse response = new DefaultApplicationAwareResponse();
        response.setApplicationInstanceIds(request.getApplications());
        String requestId = request.getId();
        response.setRequestId(requestId);
        JSONObject message = requestContext.getRequestMessage();
        if (!CollectionUtils.isEmpty(request.getApplications())) {
            sendRequestToApps(channelInfo, requestId, message, request.getApplications());
            channelInfo.getChannel().flush();
            response.setErroneous(false);
        } else {
            String messageRaw = objectMapper.writeValueAsString(message);
            logger.error("Unknown client request message: {}", messageRaw);
            response.setErroneous(true);
        }
        return response;
    }
}
