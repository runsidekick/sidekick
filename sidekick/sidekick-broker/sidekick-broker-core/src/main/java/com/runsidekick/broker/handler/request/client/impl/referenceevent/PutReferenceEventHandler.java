package com.runsidekick.broker.handler.request.client.impl.referenceevent;

import com.runsidekick.audit.logger.annotations.Audit;
import com.runsidekick.audit.logger.services.AuditLogService;
import com.runsidekick.broker.handler.request.RequestContext;
import com.runsidekick.broker.handler.request.client.impl.BaseClientRequestHandler;
import com.runsidekick.broker.model.BaseProbe;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.model.request.impl.refereceevent.PutReferenceEventRequest;
import com.runsidekick.broker.model.response.impl.referenceevent.PutReferenceEventResponse;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.LogPointService;
import com.runsidekick.broker.service.ReferenceEventService;
import com.runsidekick.broker.service.TracePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.runsidekick.broker.error.ErrorCodes.PUT_REFERENCE_EVENT_FAILED;

/**
 * @author yasin.kalafat
 */
@Component
public class PutReferenceEventHandler
        extends BaseClientRequestHandler<PutReferenceEventRequest, PutReferenceEventResponse> {

    public static final String REQUEST_NAME = "PutReferenceEventRequest";

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ReferenceEventService referenceEventService;

    @Autowired
    private TracePointService tracePointService;

    @Autowired
    private LogPointService logPointService;

    protected PutReferenceEventHandler() {
        super(REQUEST_NAME, PutReferenceEventRequest.class, PutReferenceEventResponse.class);
    }

    @Override
    @Audit(action = "PUT_REFERENCE_EVENT", domain = "REFERENCE_EVENT")
    public PutReferenceEventResponse handleRequest(ChannelInfo channelInfo,
                                                   PutReferenceEventRequest request,
                                                   RequestContext requestContext) {

        PutReferenceEventResponse putReferenceEventResponse = new PutReferenceEventResponse();
        putReferenceEventResponse.setRequestId(request.getId());
        putReferenceEventResponse.setErroneous(false);

        try {
            BaseProbe probe = null;
            // Only Predefined Probes
            if (request.getProbeType().equals(ProbeType.TRACEPOINT)) {
                probe = tracePointService.getTracePointById(request.getProbeId());
            } else if (request.getProbeType().equals(ProbeType.LOGPOINT)) {
                probe = logPointService.getLogPointById(request.getProbeId());
            }

            if (probe != null && probe.isPredefined()) {
                referenceEventService.putReferenceEvent(ReferenceEvent.builder()
                        .probeId(request.getProbeId())
                        .probeType(request.getProbeType())
                        .event(request.getEvent())
                        .build());

                auditLogService.getCurrentAuditLog().ifPresent(
                        auditLog -> {
                            setAuditLogUserInfo(auditLog, channelInfo, request.getClient());
                            auditLog.addAuditLogField("probeId", request.getProbeId());
                            auditLog.addAuditLogField("probeType", request.getProbeType().name());
                            auditLog.addAuditLogField("event", request.getEvent());
                        });
            } else {
                putReferenceEventResponse.setError(
                        PUT_REFERENCE_EVENT_FAILED, request.getProbeId(), request.getProbeType(),
                        request.getClient(), "No Predefined Probe Found");
            }
        } catch (Exception e) {
            putReferenceEventResponse.setErroneous(true);
            putReferenceEventResponse.setErrorMessage(e.getMessage());
        }

        return putReferenceEventResponse;
    }
}
