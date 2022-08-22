package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.request.impl.logpoint.ListLogPointsRequest;
import com.runsidekick.broker.model.response.impl.logpoint.ListLogPointsResponse;
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
public class ListLogPointsRequestHandler
        extends BaseClientRequestHandler<ListLogPointsRequest, ListLogPointsResponse> {

    public static final String REQUEST_NAME = "ListLogPointsRequest";

    @Autowired
    private LogPointService logPointService;

    public ListLogPointsRequestHandler() {
        super(REQUEST_NAME, ListLogPointsRequest.class, ListLogPointsResponse.class);
    }

    @Override
    public ListLogPointsResponse handleRequest(ChannelInfo channelInfo,
                                                 ListLogPointsRequest request,
                                                 RequestContext requestContext) {
        ListLogPointsResponse response = new ListLogPointsResponse();

        Collection<LogPoint> logPoints =
                logPointService.listLogPoints(
                        channelInfo.getWorkspaceId(),
                        channelInfo.getUserId());
        response.setLogPoints((List<LogPoint>) logPoints);
        response.setRequestId(request.getId());

        return response;
    }

}
