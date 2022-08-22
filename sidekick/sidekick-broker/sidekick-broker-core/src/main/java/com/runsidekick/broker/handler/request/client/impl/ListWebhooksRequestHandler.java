package com.runsidekick.broker.handler.request.client.impl;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.ListWebhooksRequest;
import com.runsidekick.broker.model.response.impl.ListWebhooksResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.model.dto.WebhookDto;
import com.runsidekick.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yasin.kalafat
 */
@Component
public class ListWebhooksRequestHandler
        extends BaseClientRequestHandler<ListWebhooksRequest, ListWebhooksResponse> {

    public static final String REQUEST_NAME = "ListWebhooksRequest";

    @Autowired
    private WebhookService webhookService;


    public ListWebhooksRequestHandler() {
        super(REQUEST_NAME, ListWebhooksRequest.class, ListWebhooksResponse.class);
    }

    @Override
    public ListWebhooksResponse handleRequest(ChannelInfo channelInfo,
                                              ListWebhooksRequest request, RequestContext requestContext) {
        List<WebhookDto> webhooks = webhookService.listByWorkspaceId(channelInfo.getWorkspaceId())
                .stream().map(WebhookDto::convert).collect(Collectors.toList());

        ListWebhooksResponse listWebhooksResponse = new ListWebhooksResponse();
        listWebhooksResponse.setRequestId(request.getId());
        listWebhooksResponse.setWebhooks(webhooks);

        return listWebhooksResponse;
    }

}