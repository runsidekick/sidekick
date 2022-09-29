package com.runsidekick.broker.handler.request.client.impl.tag;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.request.impl.tag.DisableTagRequest;
import com.runsidekick.broker.model.response.impl.tag.DisableTagResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yasin.kalafat
 */
public class DisableTagRequestHandler
        extends BaseClientRequestHandler<DisableTagRequest, DisableTagResponse> {

    @Autowired
    private AuditLogService auditLogService;

    public static final String REQUEST_NAME = "DisableTagRequest";

    public DisableTagRequestHandler() {
        super(REQUEST_NAME, DisableTagRequest.class, DisableTagResponse.class);
    }

    @Override
    @Audit(action = "DISABLE_TAG", domain = "TAG")
    public DisableTagResponse handleRequest(ChannelInfo channelInfo,
                                            DisableTagRequest request,
                                            RequestContext requestContext) {
        DisableTagResponse disableTagResponse = new DisableTagResponse();

        disableTagResponse.setApplicationInstanceIds(request.getApplications());
        disableTagResponse.setRequestId(request.getId());
        disableTagResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), request.getApplications());

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tag", request.getTag());
                    auditLog.addAuditLogField("applicationInstanceIds", request.getApplications());
                });
        return disableTagResponse;
    }
}
