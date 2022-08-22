package com.runsidekick.broker.handler.request.client.impl;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import com.runsidekick.broker.model.response.impl.ListApplicationsResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ClientMetadata;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author serkan.ozal
 */
@Component
public class ListApplicationsRequestHandler
        extends BaseClientRequestHandler<ListApplicationsRequest, ListApplicationsResponse> {

    public static final String REQUEST_NAME = "ListApplicationsRequest";

    public ListApplicationsRequestHandler() {
        super(REQUEST_NAME, ListApplicationsRequest.class, ListApplicationsResponse.class);
    }

    @Override
    public ListApplicationsResponse handleRequest(ChannelInfo channelInfo,
                                                  ListApplicationsRequest request,
                                                  RequestContext requestContext) {
        ClientMetadata clientMetadata = (ClientMetadata) channelInfo.getChannelMetadata();

        List<Application> applicationList = applicationService.listApplications(
                channelInfo.getWorkspaceId(), clientMetadata.getEmail(), request);

        ListApplicationsResponse listApplicationsResponse = new ListApplicationsResponse();
        listApplicationsResponse.setRequestId(request.getId());
        listApplicationsResponse.setApplications(applicationList);

        return listApplicationsResponse;
    }

}
