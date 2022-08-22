package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.tracepoint.RemoveTracePointRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.RemoveTracePointResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.TracePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ozge.lule
 */
@Component
public class RemoveTracePointRequestHandler
        extends TracePointChangeRequestHandler<RemoveTracePointRequest, RemoveTracePointResponse> {

    public static final String REQUEST_NAME = "RemoveTracePointRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private TracePointService tracePointService;

    public RemoveTracePointRequestHandler() {
        super(REQUEST_NAME, RemoveTracePointRequest.class, RemoveTracePointResponse.class);
    }

    @Override
    @Audit(action = "REMOVE_TRACEPOINT", domain = "TRACEPOINT")
    public RemoveTracePointResponse handleRequest(ChannelInfo channelInfo,
                                                  RemoveTracePointRequest request,
                                                  RequestContext requestContext) {

        RemoveTracePointResponse removeTracePointResponse = new RemoveTracePointResponse();

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request.getTracePointId(), request.getApplications()));

        if (request.isPersist() && request.getTracePointId() != null) {
            tracePointService.removeTracePoint(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getTracePointId());
        }
        removeTracePointResponse.setApplicationInstanceIds(applicationInstanceIds);
        removeTracePointResponse.setRequestId(request.getId());
        removeTracePointResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);
        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tracepointId", request.getTracePointId());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return removeTracePointResponse;
    }

}
