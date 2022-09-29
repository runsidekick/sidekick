package com.runsidekick.broker.handler.request.client.impl.tag;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.request.impl.tag.EnableTagRequest;
import com.runsidekick.broker.model.response.impl.tag.EnableTagResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yasin.kalafat
 */
public class EnableTagRequestHandler
        extends BaseClientRequestHandler<EnableTagRequest, EnableTagResponse> {

    @Autowired
    private AuditLogService auditLogService;

    public static final String REQUEST_NAME = "EnableTagRequest";

    public EnableTagRequestHandler() {
        super(REQUEST_NAME, EnableTagRequest.class, EnableTagResponse.class);
    }

    @Override
    @Audit(action = "ENABLE_TAG", domain = "TAG")
    public EnableTagResponse handleRequest(ChannelInfo channelInfo,
                                           EnableTagRequest request,
                                           RequestContext requestContext) {
        EnableTagResponse enableTagResponse = new EnableTagResponse();

        enableTagResponse.setApplicationInstanceIds(request.getApplications());
        enableTagResponse.setRequestId(request.getId());
        enableTagResponse.setErroneous(false);

        sendRequestToApps(channelInfo, request.getId(), requestContext.getRequestMessage(), request.getApplications());

        auditLogService.getCurrentAuditLog().ifPresent(
                auditLog -> {
                    setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                    auditLog.addAuditLogField("tag", request.getTag());
                    auditLog.addAuditLogField("applicationInstanceIds", request.getApplications());
                });
        return enableTagResponse;
    }
}
