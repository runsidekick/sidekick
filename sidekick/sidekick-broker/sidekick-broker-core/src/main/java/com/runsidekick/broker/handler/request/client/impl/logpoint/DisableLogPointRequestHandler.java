package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.request.impl.logpoint.DisableLogPointRequest;
import com.runsidekick.broker.model.response.impl.logpoint.DisableLogPointResponse;
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
public class DisableLogPointRequestHandler
        extends LogPointChangeRequestHandler<DisableLogPointRequest, DisableLogPointResponse> {

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private LogPointService logPointService;

    public static final String REQUEST_NAME = "DisableLogPointRequest";

    public DisableLogPointRequestHandler() {
        super(REQUEST_NAME, DisableLogPointRequest.class, DisableLogPointResponse.class);
    }

    @Override
    @Audit(action = "DISABLE_LOGPOINT", domain = "LOGPOINT")
    public DisableLogPointResponse handleRequest(ChannelInfo channelInfo,
                                                   DisableLogPointRequest request,
                                                   RequestContext requestContext) {
        DisableLogPointResponse disableLogPointResponse = new DisableLogPointResponse();
        if (request.isPersist() && request.getLogPointId() != null) {
            logPointService.enableDisableLogPoint(
                    channelInfo.getWorkspaceId(),
                    channelInfo.getUserId(),
                    request.getLogPointId(), true);

            LogPointConfig logPointConfig =
                    logPointService.getLogPoint(channelInfo.getWorkspaceId(), request.getLogPointId());
            disableLogPointResponse.setProbeConfig(logPointConfig);
        }

        List<String> applicationInstanceIds =
                new ArrayList<>(filterApplications(
                        channelInfo.getWorkspaceId(),
                        request.getLogPointId(),
                        request.getApplications()));
        disableLogPointResponse.setApplicationInstanceIds(applicationInstanceIds);
        disableLogPointResponse.setRequestId(request.getId());
        disableLogPointResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("logpointId", request.getLogPointId());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return disableLogPointResponse;
    }

}
