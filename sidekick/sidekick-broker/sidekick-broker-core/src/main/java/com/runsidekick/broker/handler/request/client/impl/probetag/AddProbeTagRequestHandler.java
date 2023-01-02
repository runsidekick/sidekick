package com.runsidekick.broker.handler.request.client.impl.probetag;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.probetag.AddProbeTagRequest;
import com.runsidekick.broker.model.response.impl.probetag.AddProbeTagResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.model.ProbeTag;
import com.runsidekick.service.ProbeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
public class AddProbeTagRequestHandler
        extends BaseProbeTagRequestHandler<AddProbeTagRequest, AddProbeTagResponse> {

    @Autowired
    private ProbeTagService probeTagService;

    public static final String REQUEST_NAME = "AddProbeTagRequest";

    public AddProbeTagRequestHandler() {
        super(REQUEST_NAME, AddProbeTagRequest.class, AddProbeTagResponse.class);
    }

    @Override
    public AddProbeTagResponse handleRequest(ChannelInfo channelInfo,
                                             AddProbeTagRequest request,
                                             RequestContext requestContext) {
        AddProbeTagResponse addProbeTagResponse = new AddProbeTagResponse();
        probeTagService.add(ProbeTag.builder()
                .workspaceId(channelInfo.getWorkspaceId())
                .tag(request.getTag())
                .build());
        return addProbeTagResponse;
    }
}
