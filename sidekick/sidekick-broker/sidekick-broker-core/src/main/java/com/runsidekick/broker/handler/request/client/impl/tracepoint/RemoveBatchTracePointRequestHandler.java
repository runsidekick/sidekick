package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.tracepoint.RemoveBatchTracePointRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.RemoveBatchTracePointResponse;
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
public class RemoveBatchTracePointRequestHandler
        extends BatchTracePointChangeRequestHandler<RemoveBatchTracePointRequest, RemoveBatchTracePointResponse> {

    public static final String REQUEST_NAME = "RemoveBatchTracePointRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private TracePointService tracePointService;

    public RemoveBatchTracePointRequestHandler() {
        super(REQUEST_NAME, RemoveBatchTracePointRequest.class, RemoveBatchTracePointResponse.class);
    }

    @Override
    @Audit(action = "REMOVE_BATCH_TRACEPOINT", domain = "TRACEPOINT")
    public RemoveBatchTracePointResponse handleRequest(ChannelInfo channelInfo,
                                                       RemoveBatchTracePointRequest request,
                                                       RequestContext requestContext) {

        RemoveBatchTracePointResponse removeBatchTracePointResponse = new RemoveBatchTracePointResponse();

        if (request.isPersist() && request.getTracePointIds() != null && !request.getTracePointIds().isEmpty()) {
            long deletedCount = tracePointService.removeTracePoints(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getTracePointIds());
            removeBatchTracePointResponse.setDeletedTracePointCount(deletedCount);
            removeBatchTracePointResponse.setUndeletedTracePointCount(request.getTracePointIds().size() - deletedCount);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(
                channelInfo.getWorkspaceId(), request.getTracePointIds()));

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);


        removeBatchTracePointResponse.setApplicationInstanceIds(applicationInstanceIds);
        removeBatchTracePointResponse.setRequestId(request.getId());
        removeBatchTracePointResponse.setErroneous(false);
        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return removeBatchTracePointResponse;
    }

}
