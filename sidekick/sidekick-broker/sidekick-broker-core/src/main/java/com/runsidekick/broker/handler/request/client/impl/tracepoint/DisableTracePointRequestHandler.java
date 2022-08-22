package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.request.impl.tracepoint.DisableTracePointRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.DisableTracePointResponse;
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
public class DisableTracePointRequestHandler
        extends TracePointChangeRequestHandler<DisableTracePointRequest, DisableTracePointResponse> {

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private TracePointService tracePointService;

    public static final String REQUEST_NAME = "DisableTracePointRequest";

    public DisableTracePointRequestHandler() {
        super(REQUEST_NAME, DisableTracePointRequest.class, DisableTracePointResponse.class);
    }

    @Override
    @Audit(action = "DISABLE_TRACEPOINT", domain = "TRACEPOINT")
    public DisableTracePointResponse handleRequest(ChannelInfo channelInfo,
                                                   DisableTracePointRequest request,
                                                   RequestContext requestContext) {
        DisableTracePointResponse disableTracePointResponse = new DisableTracePointResponse();
        if (request.isPersist() && request.getTracePointId() != null) {
            tracePointService.enableDisableTracePoint(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getTracePointId(), true);

            TracePointConfig tracePointConfig =
                    tracePointService.getTracePoint(channelInfo.getWorkspaceId(), request.getTracePointId());
            disableTracePointResponse.setProbeConfig(tracePointConfig);
        }

        List<String> applicationInstanceIds =
                new ArrayList<>(filterApplications(
                        channelInfo.getWorkspaceId(),
                        request.getTracePointId(),
                        request.getApplications()));
        disableTracePointResponse.setApplicationInstanceIds(applicationInstanceIds);
        disableTracePointResponse.setRequestId(request.getId());
        disableTracePointResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tracepointId", request.getTracePointId());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return disableTracePointResponse;
    }

}
