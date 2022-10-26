package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.request.impl.tracepoint.UpdateTracePointRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.UpdateTracePointResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.TracePointService;
import com.runsidekick.service.ProbeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ozge.lule
 */
@Component
public class UpdateTracePointRequestHandler
        extends TracePointChangeRequestHandler<UpdateTracePointRequest, UpdateTracePointResponse> {

    public static final String REQUEST_NAME = "UpdateTracePointRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private ProbeTagService probeTagService;
    @Autowired
    private TracePointService tracePointService;

    public UpdateTracePointRequestHandler() {
        super(REQUEST_NAME, UpdateTracePointRequest.class, UpdateTracePointResponse.class);
    }

    @Override
    @Audit(action = "UPDATE_TRACEPOINT", domain = "TRACEPOINT")
    public UpdateTracePointResponse handleRequest(ChannelInfo channelInfo,
                                                  UpdateTracePointRequest request,
                                                  RequestContext requestContext) {
        UpdateTracePointResponse updateTracePointResponse = new UpdateTracePointResponse();

        if (!CollectionUtils.isEmpty(request.getTags())) {
            request.setExpireCount(-1);
            request.setExpireSecs(-1);
        }

        if (request.isPersist() && request.getTracePointId() != null) {
            TracePoint tracePoint = new TracePoint();
            tracePoint.setId(request.getTracePointId());
            tracePoint.setClient(request.getClient());
            tracePoint.setLineNo(request.getLineNo());
            tracePoint.setFileName(request.getFileName());
            tracePoint.setExpireSecs(request.getExpireSecs());
            tracePoint.setExpireCount(request.getExpireCount());
            tracePoint.setTracingEnabled(request.isEnableTracing());
            tracePoint.setDisabled(request.isDisable());
            tracePoint.setConditionExpression(request.getConditionExpression());
            tracePoint.setWebhookIds(request.getWebhookIds());
            tracePoint.setProbeName(request.getProbeName());
            tracePoint.setTags(request.getTags());

            tracePointService.updateTracePoint(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getTracePointId(),
                    tracePoint);

            probeTagService.add(channelInfo.getWorkspaceId(), tracePoint.getTags());

            TracePointConfig tracePointConfig =
                    tracePointService.getTracePoint(channelInfo.getWorkspaceId(), request.getTracePointId());
            updateTracePointResponse.setProbeConfig(tracePointConfig);

        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request.getTracePointId(), request.getApplications()));
        updateTracePointResponse.setApplicationInstanceIds(new ArrayList<>(applicationInstanceIds));
        updateTracePointResponse.setRequestId(request.getId());
        updateTracePointResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tracepointConfig", updateTracePointResponse.getProbeConfig());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return updateTracePointResponse;
    }

}
