package com.runsidekick.broker.handler.request.client.impl.tracepoint;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.error.CodedException;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.request.impl.tracepoint.PutTracePointRequest;
import com.runsidekick.broker.model.response.impl.tracepoint.PutTracePointResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ChannelType;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.service.TracePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.runsidekick.broker.error.ErrorCodes.PUT_TRACEPOINT_FAILED;

/**
 * @author ozge.lule
 */
@Component
public class PutTracePointRequestHandler
        extends BaseClientRequestHandler<PutTracePointRequest, PutTracePointResponse> {

    public static final String REQUEST_NAME = "PutTracePointRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private TracePointService tracePointService;

    public PutTracePointRequestHandler() {
        super(REQUEST_NAME, PutTracePointRequest.class, PutTracePointResponse.class);
    }

    @Override
    @Audit(action = "PUT_TRACEPOINT", domain = "TRACEPOINT")
    public PutTracePointResponse handleRequest(ChannelInfo channelInfo,
                                               PutTracePointRequest request,
                                               RequestContext requestContext) {
        PutTracePointResponse putTracePointResponse = new PutTracePointResponse();
        String client = ((ClientMetadata) channelInfo.getChannelMetadata()).getEmail();

        List<String> applications = request.getApplications();
        List<ApplicationFilter> applicationFilters = request.getApplicationFilters();
        if ((applicationFilters == null || applicationFilters.isEmpty())
                && (applications == null || applications.isEmpty())) {
            putTracePointResponse.setErroneous(true);
            putTracePointResponse.setError(
                    PUT_TRACEPOINT_FAILED, request.getFileName(), request.getLineNo(),
                    client, "Select at least one application");
            return putTracePointResponse;
        }

        putTracePointResponse.setRequestId(request.getId());
        String tracePointId = generateId(request.getFileName(), request.getLineNo(), client);
        requestContext.putToRequest("tracePointId", tracePointId);
        if (request.isPersist()) {
            TracePointConfig tpc = new TracePointConfig();
            tpc.setId(tracePointId);
            tpc.setFileName(request.getFileName());
            tpc.setFileHash(request.getFileHash());
            tpc.setLineNo(request.getLineNo());
            tpc.setConditionExpression(request.getConditionExpression());
            tpc.setExpireCount(request.getExpireCount());
            tpc.setExpireSecs(request.getExpireSecs());
            tpc.setTracingEnabled(request.isEnableTracing());
            tpc.setDisabled(request.isDisable());
            tpc.setApplicationFilters(request.getApplicationFilters());
            tpc.setClient(client);
            tpc.setWebhookIds(request.getWebhookIds());
            tpc.setPredefined(request.isPredefined());
            tpc.setProbeName(request.getProbeName());

            try {
                tracePointService.putTracePoint(channelInfo.getWorkspaceId(), channelInfo.getUserId(), tpc,
                        channelInfo.getChannelType().equals(ChannelType.API));
            } catch (Exception e) {
                putTracePointResponse.setErroneous(true);
                if (e instanceof CodedException) {
                    putTracePointResponse.setErrorCode(((CodedException) e).getCode());
                    putTracePointResponse.setErrorMessage(e.getMessage());
                } else {
                    putTracePointResponse.setError(
                            PUT_TRACEPOINT_FAILED, request.getFileName(), request.getLineNo(),
                            request.getClient(), e.getMessage());
                }
                return putTracePointResponse;
            }

            TracePointConfig tracePointConfig =
                    tracePointService.getTracePoint(channelInfo.getWorkspaceId(), tracePointId);
            putTracePointResponse.setProbeConfig(tracePointConfig);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request));
        putTracePointResponse.setApplicationInstanceIds(applicationInstanceIds);
        putTracePointResponse.setRequestId(request.getId());
        putTracePointResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tracepointConfig", putTracePointResponse.getProbeConfig());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });
        return putTracePointResponse;
    }

    private Set<String> filterApplications(String workspaceId, PutTracePointRequest request) {
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
