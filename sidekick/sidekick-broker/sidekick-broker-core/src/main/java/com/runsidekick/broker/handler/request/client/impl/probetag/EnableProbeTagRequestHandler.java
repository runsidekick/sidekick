package com.runsidekick.broker.handler.request.client.impl.probetag;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.request.impl.probetag.EnableProbeTagRequest;
import com.runsidekick.broker.model.response.impl.probetag.EnableProbeTagResponse;
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
public class EnableProbeTagRequestHandler
        extends BaseProbeTagRequestHandler<EnableProbeTagRequest, EnableProbeTagResponse> {

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private LogPointService logPointService;
    @Autowired
    private TracePointService tracePointService;

    public static final String REQUEST_NAME = "EnableProbeTagRequest";

    public EnableProbeTagRequestHandler() {
        super(REQUEST_NAME, EnableProbeTagRequest.class, EnableProbeTagResponse.class);
    }

    @Override
    @Audit(action = "ENABLE_PROBE_TAG", domain = "PROBE_TAG")
    public EnableProbeTagResponse handleRequest(ChannelInfo channelInfo,
                                                EnableProbeTagRequest request,
                                                RequestContext requestContext) {
        EnableProbeTagResponse enableProbeTagResponse = new EnableProbeTagResponse();

        if (StringUtils.hasText(request.getTag())) {
            List<LogPointConfig> logPoints = logPointService.queryLogPoints(channelInfo.getWorkspaceId(),
                    request.getApplicationFilters(), request.getTag());

            List<TracePointConfig> tracePoints = tracePointService.queryTracePoints(channelInfo.getWorkspaceId(),
                    request.getApplicationFilters(), request.getTag());

            System.out.println(tracePoints.size());
            System.out.println(logPoints.size());

            if (logPoints != null && !logPoints.isEmpty()) {
                List<String> logPointIds = logPoints.stream()
                        .map(logPoint -> logPoint.getId())
                        .collect(Collectors.toList());

                logPointService.enableDisableLogPoints(channelInfo.getWorkspaceId(), logPointIds, false);
            }
            if (tracePoints != null && !tracePoints.isEmpty()) {
                List<String> tracePointIds = tracePoints.stream()
                        .map(tracePoint -> tracePoint.getId())
                        .collect(Collectors.toList());

                tracePointService.enableDisableTracePoints(channelInfo.getWorkspaceId(), tracePointIds, false);
            }
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request.getApplicationFilters()));
        enableProbeTagResponse.setApplicationInstanceIds(applicationInstanceIds);
        enableProbeTagResponse.setRequestId(request.getId());
        enableProbeTagResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tag", request.getTag());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return enableProbeTagResponse;
    }
}
