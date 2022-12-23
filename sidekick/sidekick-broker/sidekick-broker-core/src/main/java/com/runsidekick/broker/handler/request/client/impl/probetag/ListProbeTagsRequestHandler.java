package com.runsidekick.broker.handler.request.client.impl.probetag;

import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.probetag.ListProbeTagsRequest;
import com.runsidekick.broker.model.response.impl.probetag.ListProbeTagsResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.model.ProbeTag;
import com.runsidekick.service.ProbeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Component
public class ListProbeTagsRequestHandler
        extends BaseProbeTagRequestHandler<ListProbeTagsRequest, ListProbeTagsResponse> {

    @Autowired
    private ProbeTagService probeTagService;

    public static final String REQUEST_NAME = "ListProbeTagsRequest";

    public ListProbeTagsRequestHandler() {
        super(REQUEST_NAME, ListProbeTagsRequest.class, ListProbeTagsResponse.class);
    }

    @Override
    public ListProbeTagsResponse handleRequest(ChannelInfo channelInfo,
                                               ListProbeTagsRequest request,
                                                RequestContext requestContext) {
        ListProbeTagsResponse listProbeTagsResponse = new ListProbeTagsResponse();

        List<ProbeTag> probeTags = probeTagService.listByWorkspaceId(channelInfo.getWorkspaceId());

        listProbeTagsResponse.setProbeTags(probeTags);
        listProbeTagsResponse.setRequestId(request.getId());
        listProbeTagsResponse.setErroneous(false);
        return listProbeTagsResponse;
    }
}
