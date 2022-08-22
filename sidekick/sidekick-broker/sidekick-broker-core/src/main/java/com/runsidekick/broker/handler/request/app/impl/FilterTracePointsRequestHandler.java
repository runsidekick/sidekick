package com.runsidekick.broker.handler.request.app.impl;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.impl.tracepoint.FilterTracePointsRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.FilterTracePointsResponse;
import com.runsidekick.broker.proxy.ApplicationMetadata;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.TracePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author ozge.lule
 */
@Component
public class FilterTracePointsRequestHandler
        extends BaseAppRequestHandler<FilterTracePointsRequest, FilterTracePointsResponse> {

    public static final String REQUEST_NAME = "FilterTracePointsRequest";

    @Autowired
    private TracePointService tracePointService;

    public FilterTracePointsRequestHandler() {
        super(REQUEST_NAME, FilterTracePointsRequest.class, FilterTracePointsResponse.class);
    }

    @Override
    public FilterTracePointsResponse handleRequest(ChannelInfo channelInfo,
                                                   FilterTracePointsRequest request,
                                                   RequestContext requestContext) {
        FilterTracePointsResponse response = new FilterTracePointsResponse();

        Collection<TracePoint> tracePoints =
                tracePointService.queryTracePoints(channelInfo.getWorkspaceId(), request.getApplicationFilter());

        ApplicationMetadata channelMetadata = (ApplicationMetadata) channelInfo.getChannelMetadata();
        response.setApplicationInstanceId(channelMetadata.getInstanceId());
        response.setApplicationName(channelMetadata.getName());

        response.setTracePoints((List<TracePoint>) tracePoints);
        response.setRequestId(request.getId());

        return response;
    }
}
