package com.runsidekick.broker.handler.request.client.impl.probetag;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.request.impl.probetag.DisableProbeTagRequest;
import com.runsidekick.broker.model.response.impl.probetag.DisableProbeTagResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.LogPointService;
import com.runsidekick.broker.service.TracePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yasin.kalafat
 */
@Component
public class DisableProbeTagRequestHandler
        extends BaseProbeTagRequestHandler<DisableProbeTagRequest, DisableProbeTagResponse> {

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private LogPointService logPointService;
    @Autowired
    private TracePointService tracePointService;

    public static final String REQUEST_NAME = "DisableProbeTagRequest";

    public DisableProbeTagRequestHandler() {
        super(REQUEST_NAME, DisableProbeTagRequest.class, DisableProbeTagResponse.class);
    }

    @Override
    @Audit(action = "DISABLE_PROBE_TAG", domain = "PROBE_TAG")
    public DisableProbeTagResponse handleRequest(ChannelInfo channelInfo,
                                                 DisableProbeTagRequest request,
                                                 RequestContext requestContext) {
        DisableProbeTagResponse disableProbeTagResponse = new DisableProbeTagResponse();

        if (StringUtils.hasText(request.getTag())) {
            List<LogPointConfig> logPoints = logPointService.queryLogPoints(channelInfo.getWorkspaceId(),
                    request.getApplicationFilters(), request.getTag());

            List<TracePointConfig> tracePoints = tracePointService.queryTracePoints(channelInfo.getWorkspaceId(),
                    request.getApplicationFilters(), request.getTag());

            if (logPoints != null && !logPoints.isEmpty()) {
                List<String> logPointIds = logPoints.stream()
                        .map(logPoint -> logPoint.getId())
                        .collect(Collectors.toList());

                logPointService.enableDisableLogPoints(channelInfo.getWorkspaceId(), logPointIds, true);
            }
            if (tracePoints != null && !tracePoints.isEmpty()) {
                List<String> tracePointIds = tracePoints.stream()
                        .map(tracePoint -> tracePoint.getId())
                        .collect(Collectors.toList());

                tracePointService.enableDisableTracePoints(channelInfo.getWorkspaceId(), tracePointIds, true);
            }
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request.getApplicationFilters()));
        disableProbeTagResponse.setApplicationInstanceIds(applicationInstanceIds);
        disableProbeTagResponse.setRequestId(request.getId());
        disableProbeTagResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tag", request.getTag());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return disableProbeTagResponse;
    }
}
