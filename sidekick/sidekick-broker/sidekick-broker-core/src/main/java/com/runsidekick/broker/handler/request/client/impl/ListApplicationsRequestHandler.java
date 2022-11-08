package com.runsidekick.broker.handler.request.client.impl;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.Application;
import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.request.impl.ListApplicationsRequest;
import com.runsidekick.broker.model.response.impl.ListApplicationsResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.service.ApplicationConfigService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author serkan.ozal
 */
@Component
public class ListApplicationsRequestHandler
        extends BaseClientRequestHandler<ListApplicationsRequest, ListApplicationsResponse> {

    public static final String REQUEST_NAME = "ListApplicationsRequest";

    private final ApplicationConfigService applicationConfigService;

    public ListApplicationsRequestHandler(ApplicationConfigService applicationConfigService) {
        super(REQUEST_NAME, ListApplicationsRequest.class, ListApplicationsResponse.class);
        this.applicationConfigService = applicationConfigService;
    }

    @Override
    public ListApplicationsResponse handleRequest(ChannelInfo channelInfo,
                                                  ListApplicationsRequest request,
                                                  RequestContext requestContext) {
        ClientMetadata clientMetadata = (ClientMetadata) channelInfo.getChannelMetadata();

        List<Application> applicationList = applicationService.listApplications(
                channelInfo.getWorkspaceId(), clientMetadata.getEmail(), request);

        List<ApplicationConfig> applicationConfigs =
                applicationConfigService.listApplicationConfigs(channelInfo.getWorkspaceId());

        ListApplicationsResponse listApplicationsResponse = new ListApplicationsResponse();
        listApplicationsResponse.setRequestId(request.getId());
        listApplicationsResponse.setApplications(applicationList);
        listApplicationsResponse.setApplicationConfigs(applicationConfigs);

        return listApplicationsResponse;
    }

}
