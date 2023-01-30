package com.runsidekick.broker.handler.request.client.impl.probetag;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.probetag.RemoveProbeTagRequest;
import com.runsidekick.broker.model.response.impl.probetag.RemoveProbeTagResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.LogPointService;
import com.runsidekick.broker.service.TracePointService;
import com.runsidekick.model.ProbeTag;
import com.runsidekick.service.ProbeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Component
public class RemoveProbeTagRequestHandler
        extends BaseProbeTagRequestHandler<RemoveProbeTagRequest, RemoveProbeTagResponse> {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private LogPointService logPointService;

    @Autowired
    private ProbeTagService probeTagService;

    @Autowired
    private TracePointService tracePointService;

    public static final String REQUEST_NAME = "RemoveProbeTagRequest";

    public RemoveProbeTagRequestHandler() {
        super(REQUEST_NAME, RemoveProbeTagRequest.class, RemoveProbeTagResponse.class);
    }

    @Override
    @Audit(action = "REMOVE_TAG", domain = "PROBE_TAG")
    public RemoveProbeTagResponse handleRequest(ChannelInfo channelInfo,
                                                RemoveProbeTagRequest request,
                                                RequestContext requestContext) {
        RemoveProbeTagResponse removeProbeTagResponse = new RemoveProbeTagResponse();
        ProbeTag probeTag = probeTagService.getByWorkspaceId(channelInfo.getWorkspaceId(), request.getTag());
        if (probeTag != null) {
            probeTagService.delete(probeTag.getId());
            logPointService.deleteTag(channelInfo.getWorkspaceId(), request.getTag());
            tracePointService.deleteTag(channelInfo.getWorkspaceId(), request.getTag());
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId()));
        removeProbeTagResponse.setApplicationInstanceIds(applicationInstanceIds);
        removeProbeTagResponse.setRequestId(request.getId());
        removeProbeTagResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tag", request.getTag());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return removeProbeTagResponse;
    }
}
