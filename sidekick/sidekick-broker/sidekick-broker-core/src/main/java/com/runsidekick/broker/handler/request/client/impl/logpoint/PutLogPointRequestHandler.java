package com.runsidekick.broker.handler.request.client.impl.logpoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.error.CodedException;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.request.impl.logpoint.PutLogPointRequest;
import com.runsidekick.broker.model.response.impl.logpoint.PutLogPointResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ChannelType;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.service.LogPointService;
import com.runsidekick.service.ServerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.runsidekick.broker.error.ErrorCodes.PUT_LOGPOINT_FAILED;

/**
 * @author yasin.kalafat
 */
@Component
public class PutLogPointRequestHandler
        extends BaseClientRequestHandler<PutLogPointRequest, PutLogPointResponse> {

    public static final String REQUEST_NAME = "PutLogPointRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private LogPointService logPointService;
    @Autowired
    private ServerStatisticsService serverStatisticsService;

    public PutLogPointRequestHandler() {
        super(REQUEST_NAME, PutLogPointRequest.class, PutLogPointResponse.class);
    }

    @Override
    @Audit(action = "PUT_LOGPOINT", domain = "LOGPOINT")
    public PutLogPointResponse handleRequest(ChannelInfo channelInfo,
                                               PutLogPointRequest request,
                                               RequestContext requestContext) {
        PutLogPointResponse putLogPointResponse = new PutLogPointResponse();
        String client = ((ClientMetadata) channelInfo.getChannelMetadata()).getEmail();

        List<String> applications = request.getApplications();
        List<ApplicationFilter> applicationFilters = request.getApplicationFilters();
        if ((applicationFilters == null || applicationFilters.isEmpty())
                && (applications == null || applications.isEmpty())) {
            putLogPointResponse.setErroneous(true);
            putLogPointResponse.setError(
                    PUT_LOGPOINT_FAILED, request.getFileName(), request.getLineNo(),
                    client, "Select at least one application");
            return putLogPointResponse;
        }

        putLogPointResponse.setRequestId(request.getId());
        String logPointId = generateId(request.getFileName(), request.getLineNo(), client);
        requestContext.putToRequest("logPointId", logPointId);
        if (request.isPersist()) {
            LogPointConfig lpc = new LogPointConfig();
            lpc.setId(logPointId);
            lpc.setLogExpression(request.getLogExpression());
            lpc.setStdoutEnabled(request.isStdoutEnabled());
            lpc.setLogLevel(request.getLogLevel());
            lpc.setFileName(request.getFileName());
            lpc.setFileHash(request.getFileHash());
            lpc.setLineNo(request.getLineNo());
            lpc.setConditionExpression(request.getConditionExpression());
            lpc.setExpireCount(request.getExpireCount());
            lpc.setExpireSecs(request.getExpireSecs());
            lpc.setDisabled(request.isDisable());
            lpc.setApplicationFilters(request.getApplicationFilters());
            lpc.setClient(client);
            lpc.setWebhookIds(request.getWebhookIds());
            lpc.setPredefined(request.isPredefined());
            lpc.setProbeName(request.getProbeName());
            if (lpc.isPredefined()) {
                lpc.setTags(request.getTags());
            }


            try {
                logPointService.putLogPoint(channelInfo.getWorkspaceId(), channelInfo.getUserId(), lpc,
                        channelInfo.getChannelType().equals(ChannelType.API));
                serverStatisticsService.increaseLogPointCount(channelInfo.getWorkspaceId());
            } catch (Exception e) {
                putLogPointResponse.setErroneous(true);
                if (e instanceof CodedException) {
                    putLogPointResponse.setErrorCode(((CodedException) e).getCode());
                    putLogPointResponse.setErrorMessage(e.getMessage());
                } else {
                    putLogPointResponse.setError(
                            PUT_LOGPOINT_FAILED, request.getFileName(), request.getLineNo(),
                            request.getClient(), e.getMessage());
                }
                return putLogPointResponse;
            }

            LogPointConfig logPointConfig =
                    logPointService.getLogPoint(channelInfo.getWorkspaceId(), logPointId);
            putLogPointResponse.setProbeConfig(logPointConfig);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request));
        putLogPointResponse.setApplicationInstanceIds(applicationInstanceIds);
        putLogPointResponse.setRequestId(request.getId());
        putLogPointResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("logpointConfig", putLogPointResponse.getProbeConfig());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return putLogPointResponse;
    }

    private Set<String> filterApplications(String workspaceId, PutLogPointRequest request) {
        Set<String> apps = new HashSet<>();

        if (request.getApplications() != null) {
            apps.addAll(request.getApplications());
        }
        apps.addAll(applicationService.filterApplications(workspaceId, request.getApplicationFilters()));
        return apps;
    }

    private static String generateId(String fileName, int lineNo, String client) {
        return fileName + "::" + lineNo + "::" + client;
    }

}
