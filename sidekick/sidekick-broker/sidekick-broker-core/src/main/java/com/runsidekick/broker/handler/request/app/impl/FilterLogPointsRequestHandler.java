package com.runsidekick.broker.handler.request.app.impl;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.request.impl.logpoint.FilterLogPointsRequest;
import com.runsidekick.broker.model.response.impl.logpoint.FilterLogPointsResponse;
import com.runsidekick.broker.proxy.ApplicationMetadata;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.LogPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Component
public class FilterLogPointsRequestHandler
        extends BaseAppRequestHandler<FilterLogPointsRequest, FilterLogPointsResponse> {

    public static final String REQUEST_NAME = "FilterLogPointsRequest";

    @Autowired
    private LogPointService logPointService;

    public FilterLogPointsRequestHandler() {
        super(REQUEST_NAME, FilterLogPointsRequest.class, FilterLogPointsResponse.class);
    }

    @Override
    public FilterLogPointsResponse handleRequest(ChannelInfo channelInfo,
                                                 FilterLogPointsRequest request,
                                                   RequestContext requestContext) {
        FilterLogPointsResponse response = new FilterLogPointsResponse();

        Collection<LogPoint> logPoints =
                logPointService.queryLogPoints(channelInfo.getWorkspaceId(), request.getApplicationFilter());

        ApplicationMetadata channelMetadata = (ApplicationMetadata) channelInfo.getChannelMetadata();
        response.setApplicationInstanceId(channelMetadata.getInstanceId());
        response.setApplicationName(channelMetadata.getName());

        response.setLogPoints((List<LogPoint>) logPoints);
        response.setRequestId(request.getId());

        return response;
    }
}
