package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.request.impl.tracepoint.EnableTracePointRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.EnableTracePointResponse;
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
public class EnableTracePointRequestHandler
        extends TracePointChangeRequestHandler<EnableTracePointRequest, EnableTracePointResponse> {

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private TracePointService tracePointService;

    public static final String REQUEST_NAME = "EnableTracePointRequest";

    public EnableTracePointRequestHandler() {
        super(REQUEST_NAME, EnableTracePointRequest.class, EnableTracePointResponse.class);
    }

    @Override
    @Audit(action = "ENABLE_TRACEPOINT", domain = "TRACEPOINT")
    public EnableTracePointResponse handleRequest(ChannelInfo channelInfo,
                                                  EnableTracePointRequest request,
                                                  RequestContext requestContext) {

        EnableTracePointResponse enableTracePointResponse = new EnableTracePointResponse();

        if (request.isPersist() && request.getTracePointId() != null) {
            tracePointService.enableDisableTracePoint(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getTracePointId(), false);

            TracePointConfig tracePointConfig =
                    tracePointService.getTracePoint(channelInfo.getWorkspaceId(), request.getTracePointId());
            enableTracePointResponse.setProbeConfig(tracePointConfig);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request.getTracePointId(), request.getApplications()));
        enableTracePointResponse.setApplicationInstanceIds(applicationInstanceIds);
        enableTracePointResponse.setRequestId(request.getId());
        enableTracePointResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tracepointId", request.getTracePointId());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return enableTracePointResponse;
    }

}
