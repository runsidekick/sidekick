package com.runsidekick.broker.handler.request.client.impl.referenceevent;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.model.request.impl.refereceevent.GetReferenceEventRequest;
import com.runsidekick.broker.model.response.impl.referenceevent.GetReferenceEventResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.ReferenceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
public class GetReferenceEventHandler
        extends BaseClientRequestHandler<GetReferenceEventRequest, GetReferenceEventResponse> {

    public static final String REQUEST_NAME = "GetReferenceEventRequest";

    @Autowired
    private ReferenceEventService referenceEventService;

    protected GetReferenceEventHandler() {
        super(REQUEST_NAME, GetReferenceEventRequest.class, GetReferenceEventResponse.class);
    }

    @Override
    public GetReferenceEventResponse handleRequest(ChannelInfo channelInfo,
                                                   GetReferenceEventRequest request,
                                                   RequestContext requestContext) {
        ReferenceEvent referenceEvent = referenceEventService.getReferenceEvent(
                request.getProbeId(), request.getProbeType());

        GetReferenceEventResponse response = new GetReferenceEventResponse();
        response.setRequestId(request.getId());
        response.setReferenceEvent(referenceEvent);
        return response;
    }
}
