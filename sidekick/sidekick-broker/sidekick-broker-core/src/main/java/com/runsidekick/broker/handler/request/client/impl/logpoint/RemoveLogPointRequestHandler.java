package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.request.impl.logpoint.RemoveLogPointRequest;
import com.runsidekick.broker.model.response.impl.logpoint.RemoveLogPointResponse;
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
public class RemoveLogPointRequestHandler
        extends LogPointChangeRequestHandler<RemoveLogPointRequest, RemoveLogPointResponse> {

    public static final String REQUEST_NAME = "RemoveLogPointRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private LogPointService logPointService;

    public RemoveLogPointRequestHandler() {
        super(REQUEST_NAME, RemoveLogPointRequest.class, RemoveLogPointResponse.class);
    }

    @Override
    @Audit(action = "REMOVE_LOGPOINT", domain = "LOGPOINT")
    public RemoveLogPointResponse handleRequest(ChannelInfo channelInfo,
                                                  RemoveLogPointRequest request,
                                                  RequestContext requestContext) {

        RemoveLogPointResponse removeLogPointResponse = new RemoveLogPointResponse();

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request.getLogPointId(), request.getApplications()));

        if (request.isPersist() && request.getLogPointId() != null) {
            logPointService.removeLogPoint(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getLogPointId());
        }
        removeLogPointResponse.setApplicationInstanceIds(applicationInstanceIds);
        removeLogPointResponse.setRequestId(request.getId());
        removeLogPointResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);
        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("logpointId", request.getLogPointId());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return removeLogPointResponse;
    }

}
