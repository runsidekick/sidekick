package com.runsidekick.broker.handler.request.client.impl.referenceevent;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.request.impl.refereceevent.RemoveReferenceEventRequest;
import com.runsidekick.broker.model.response.impl.referenceevent.RemoveReferenceEventResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.ReferenceEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
public class RemoveReferenceEventHandler
        extends BaseClientRequestHandler<RemoveReferenceEventRequest, RemoveReferenceEventResponse> {

    public static final String REQUEST_NAME = "RemoveReferenceEventRequest";

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ReferenceEventService referenceEventService;

    protected RemoveReferenceEventHandler() {
        super(REQUEST_NAME, RemoveReferenceEventRequest.class, RemoveReferenceEventResponse.class);
    }

    @Override
    @Audit(action = "REMOVE_REFERENCE_EVENT", domain = "REFERENCE_EVENT")
    public RemoveReferenceEventResponse handleRequest(ChannelInfo channelInfo,
                                                   RemoveReferenceEventRequest request,
                                                   RequestContext requestContext) {

        RemoveReferenceEventResponse removeReferenceEventResponse = new RemoveReferenceEventResponse();
        removeReferenceEventResponse.setRequestId(request.getId());
        removeReferenceEventResponse.setErroneous(false);

        try {
            referenceEventService.removeReferenceEvent(request.getProbeId(), request.getProbeType());
            auditLogService.getCurrentAuditLog().ifPresent(
                    auditLog -> {
                        setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                        auditLog.addAuditLogField("probeId", request.getProbeId());
                        auditLog.addAuditLogField("probeType", request.getProbeType().name());
                    });
        } catch (Exception e) {
            removeReferenceEventResponse.setErroneous(true);
            removeReferenceEventResponse.setErrorMessage(e.getMessage());
        }

        return removeReferenceEventResponse;
    }
}
