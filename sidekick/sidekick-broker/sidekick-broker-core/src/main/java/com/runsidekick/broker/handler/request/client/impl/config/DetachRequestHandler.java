package com.runsidekick.broker.handler.request.client.impl.config;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.config.DetachRequest;
import com.runsidekick.broker.model.response.impl.config.DetachResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.service.ApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.runsidekick.broker.error.ErrorCodes.DETACH_FAILED;

/**
 * @author yasin.kalafat
 */
@Component
public class DetachRequestHandler extends BaseApplicationConfigRequestHandler<DetachRequest, DetachResponse> {

    public static final String REQUEST_NAME = "DetachRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private ApplicationConfigService applicationConfigService;

    public DetachRequestHandler() {
        super(REQUEST_NAME, DetachRequest.class, DetachResponse.class);
    }

    @Override
    @Audit(action = "DETACH", domain = "APP_CONFIG")
    public DetachResponse handleRequest(ChannelInfo channelInfo, DetachRequest request, RequestContext requestContext) {
        DetachResponse detachResponse = new DetachResponse();
        String client = ((ClientMetadata) channelInfo.getChannelMetadata()).getEmail();

        List<String> applications = request.getApplications();
        List<ApplicationFilter> applicationFilters = request.getApplicationFilters();
        if ((applicationFilters == null || applicationFilters.isEmpty())
                && (applications == null || applications.isEmpty())) {
            detachResponse.setErroneous(true);
            detachResponse.setError(
                    DETACH_FAILED, client, "Select at least one application");
            return detachResponse;
        }

        for (ApplicationFilter applicationFilter: applicationFilters) {
            applicationConfigService.detachApplication(channelInfo.getWorkspaceId(), applicationFilter);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request));
        detachResponse.setApplicationInstanceIds(applicationInstanceIds);
        detachResponse.setRequestId(request.getId());
        detachResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });

        return detachResponse;
    }
}
