package com.runsidekick.broker.handler.request.client.impl.config;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.model.ApplicationConfig;
import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.request.impl.config.UpdateConfigRequest;
import com.runsidekick.broker.model.response.impl.config.UpdateConfigResponse;
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
public class UpdateConfigRequestHandler extends
        BaseApplicationConfigRequestHandler<UpdateConfigRequest, UpdateConfigResponse> {

    public static final String REQUEST_NAME = "UpdateConfigRequest";

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private ApplicationConfigService applicationConfigService;

    public UpdateConfigRequestHandler() {
        super(REQUEST_NAME, UpdateConfigRequest.class, UpdateConfigResponse.class);
    }

    @Override
    @Audit(action = "SET_CONFIG", domain = "APP_CONFIG")
    public UpdateConfigResponse handleRequest(
            ChannelInfo channelInfo, UpdateConfigRequest request, RequestContext requestContext) {
        UpdateConfigResponse updateConfigResponse = new UpdateConfigResponse();
        String client = ((ClientMetadata) channelInfo.getChannelMetadata()).getEmail();

        List<String> applications = request.getApplications();
        List<ApplicationFilter> applicationFilters = request.getApplicationFilters();
        if ((applicationFilters == null || applicationFilters.isEmpty())
                && (applications == null || applications.isEmpty())) {
            updateConfigResponse.setErroneous(true);
            updateConfigResponse.setError(
                    DETACH_FAILED, client, "Select at least one application");
            return updateConfigResponse;
        }

        for (ApplicationFilter applicationFilter: applicationFilters) {
            applicationConfigService.saveApplicationConfig(ApplicationConfig.builder()
                            .workspaceId(channelInfo.getWorkspaceId())
                            .applicationFilter(applicationFilter)
                            .config(request.getConfig())
                    .build());
        }

        List<String> applicationInstanceIds = new ArrayList<>(filterApplications(channelInfo.getWorkspaceId(),
                request));
        updateConfigResponse.setApplicationInstanceIds(applicationInstanceIds);
        updateConfigResponse.setRequestId(request.getId());
        updateConfigResponse.setErroneous(false);
        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), applicationInstanceIds);

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("config", request.getConfig());
                    auditLog.addAuditLogField("applicationInstanceIds", applicationInstanceIds);
                });

        return updateConfigResponse;
    }
}
