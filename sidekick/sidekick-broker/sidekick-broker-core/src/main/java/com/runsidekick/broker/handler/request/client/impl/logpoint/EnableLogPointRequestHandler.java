package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.request.impl.logpoint.EnableLogPointRequest;
import com.runsidekick.broker.model.response.impl.logpoint.EnableLogPointResponse;
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
public class EnableLogPointRequestHandler
        extends LogPointChangeRequestHandler<EnableLogPointRequest, EnableLogPointResponse> {

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private LogPointService logPointService;

    public static final String REQUEST_NAME = "EnableLogPointRequest";

    public EnableLogPointRequestHandler() {
        super(REQUEST_NAME, EnableLogPointRequest.class, EnableLogPointResponse.class);
    }

    @Override
    @Audit(action = "ENABLE_LOGPOINT", domain = "LOGPOINT")
    public EnableLogPointResponse handleRequest(ChannelInfo channelInfo,
                                                  EnableLogPointRequest request,
                                                  RequestContext requestContext) {

        EnableLogPointResponse enableLogPointResponse = new EnableLogPointResponse();

        if (request.isPersist() && request.getLogPointId() != null) {
            logPointService.enableDisableLogPoint(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getLogPointId(), false);

            LogPointConfig logPointConfig =
                    logPointService.getLogPoint(channelInfo.getWorkspaceId(), request.getLogPointId());
            enableLogPointResponse.setProbeConfig(logPointConfig);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request.getLogPointId(), request.getApplications()));
        enableLogPointResponse.setApplicationInstanceIds(applicationInstanceIds);
        enableLogPointResponse.setRequestId(request.getId());
        enableLogPointResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("logpointId", request.getLogPointId());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return enableLogPointResponse;
    }

}
