package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.request.impl.tracepoint.ListTracePointsRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.ListTracePointsResponse;
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
public class ListTracePointsRequestHandler
        extends BaseClientRequestHandler<ListTracePointsRequest, ListTracePointsResponse> {

    public static final String REQUEST_NAME = "ListTracePointsRequest";

    @Autowired
    private TracePointService tracePointService;

    public ListTracePointsRequestHandler() {
        super(REQUEST_NAME, ListTracePointsRequest.class, ListTracePointsResponse.class);
    }

    @Override
    public ListTracePointsResponse handleRequest(ChannelInfo channelInfo,
                                                 ListTracePointsRequest request,
                                                 RequestContext requestContext) {
        ListTracePointsResponse response = new ListTracePointsResponse();

        Collection<TracePoint> tracePoints =
                tracePointService.listTracePoints(
                        channelInfo.getWorkspaceId(),
                        channelInfo.getUserId());
        response.setTracePoints((List<TracePoint>) tracePoints);
        response.setRequestId(request.getId());

        return response;
    }

}
