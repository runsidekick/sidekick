package com.runsidekick.broker.handler.request.client.impl.probetag;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.probetag.RemoveProbeTagRequest;
import com.runsidekick.broker.model.response.impl.probetag.RemoveProbeTagResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.model.ProbeTag;
import com.runsidekick.service.ProbeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
public class RemoveProbeTagRequestHandler
        extends BaseProbeTagRequestHandler<RemoveProbeTagRequest, RemoveProbeTagResponse> {

    @Autowired
    private ProbeTagService probeTagService;

    public static final String REQUEST_NAME = "RemoveProbeTagRequest";

    public RemoveProbeTagRequestHandler() {
        super(REQUEST_NAME, RemoveProbeTagRequest.class, RemoveProbeTagResponse.class);
    }

    @Override
    public RemoveProbeTagResponse handleRequest(ChannelInfo channelInfo,
                                                RemoveProbeTagRequest request,
                                                RequestContext requestContext) {
        RemoveProbeTagResponse removeProbeTagResponse = new RemoveProbeTagResponse();
        ProbeTag probeTag = probeTagService.getByWorkspaceId(channelInfo.getWorkspaceId(), request.getTag());
        if (probeTag != null) {
            probeTagService.delete(probeTag.getId());
            // TODO what will happen to probes with this tag?
        }
        return removeProbeTagResponse;
    }
}
