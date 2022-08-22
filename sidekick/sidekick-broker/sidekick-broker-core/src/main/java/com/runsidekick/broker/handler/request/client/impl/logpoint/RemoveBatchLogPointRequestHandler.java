package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.logpoint.RemoveBatchLogPointRequest;
import com.runsidekick.broker.model.response.impl.logpoint.RemoveBatchLogPointResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.LogPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Component
public class RemoveBatchLogPointRequestHandler
        extends BatchLogPointChangeRequestHandler<RemoveBatchLogPointRequest, RemoveBatchLogPointResponse> {

    public static final String REQUEST_NAME = "RemoveBatchLogPointRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private LogPointService logPointService;

    public RemoveBatchLogPointRequestHandler() {
        super(REQUEST_NAME, RemoveBatchLogPointRequest.class, RemoveBatchLogPointResponse.class);
    }

    @Override
    @Audit(action = "REMOVE_BATCH_LOGPOINT", domain = "LOGPOINT")
    public RemoveBatchLogPointResponse handleRequest(ChannelInfo channelInfo,
                                                       RemoveBatchLogPointRequest request,
                                                       RequestContext requestContext) {

        RemoveBatchLogPointResponse removeBatchLogPointResponse = new RemoveBatchLogPointResponse();

        if (request.isPersist() && request.getLogPointIds() != null && !request.getLogPointIds().isEmpty()) {
            long deletedCount = logPointService.removeLogPoints(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getLogPointIds());
            removeBatchLogPointResponse.setDeletedLogPointCount(deletedCount);
            removeBatchLogPointResponse.setUndeletedLogPointCount(request.getLogPointIds().size() - deletedCount);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(
                channelInfo.getWorkspaceId(), request.getLogPointIds()));

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);


        removeBatchLogPointResponse.setApplicationInstanceIds(applicationInstanceIds);
        removeBatchLogPointResponse.setRequestId(request.getId());
        removeBatchLogPointResponse.setErroneous(false);
        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return removeBatchLogPointResponse;
    }

}
