package com.runsidekick.broker.handler.request.client.impl.config;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.config.AttachRequest;
import com.runsidekick.broker.model.response.impl.config.AttachResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.proxy.ClientMetadata;
import com.runsidekick.broker.service.ApplicationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.runsidekick.broker.error.ErrorCodes.ATTACH_FAILED;

/**
 * @author yasin.kalafat
 */
@Component
public class AttachRequestHandler extends BaseApplicationConfigRequestHandler<AttachRequest, AttachResponse> {

    public static final String REQUEST_NAME = "AttachRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private ApplicationConfigService applicationConfigService;

    public AttachRequestHandler() {
        super(REQUEST_NAME, AttachRequest.class, AttachResponse.class);
    }

    @Override
    @Audit(action = "ATTACH", domain = "APP_CONFIG")
    public AttachResponse handleRequest(ChannelInfo channelInfo, AttachRequest request, RequestContext requestContext) {
        AttachResponse attachResponse = new AttachResponse();
        String client = ((ClientMetadata) channelInfo.getChannelMetadata()).getEmail();

        List<String> applications = request.getApplications();
        List<ApplicationFilter> applicationFilters = request.getApplicationFilters();
        if ((applicationFilters == null || applicationFilters.isEmpty())
                && (applications == null || applications.isEmpty())) {
            attachResponse.setErroneous(true);
            attachResponse.setError(
                    ATTACH_FAILED, client, "Select at least one application");
            return attachResponse;
        }

        for (ApplicationFilter applicationFilter: applicationFilters) {
            applicationConfigService.attachApplication(channelInfo.getWorkspaceId(), applicationFilter);
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request));
        attachResponse.setApplicationInstanceIds(applicationInstanceIds);
        attachResponse.setRequestId(request.getId());
        attachResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });

        return attachResponse;
    }
}
