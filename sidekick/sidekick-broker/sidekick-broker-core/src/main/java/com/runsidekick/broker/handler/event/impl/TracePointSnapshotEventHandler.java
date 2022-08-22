package com.runsidekick.broker.handler.event.impl;

import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.model.NotificationType;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.model.event.EventAck;
import com.runsidekick.broker.model.event.impl.TracePointSnapshotEvent;
import com.runsidekick.broker.model.request.impl.tracepoint.RemoveTracePointRequest;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.TracePointService;
import io.thundra.swark.utils.UUIDUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author tolgatakir
 */
@Component
public class TracePointSnapshotEventHandler extends BaseProbeEventHandler<TracePoint, TracePointSnapshotEvent> {
    private static final String EVENT_NAME = "TracePointSnapshotEvent";

    private final TracePointService tracePointService;

    public TracePointSnapshotEventHandler(TracePointService tracePointService) {
        super(EVENT_NAME, TracePointSnapshotEvent.class, NotificationType.USER_DEBUGGER_FIRST_TRACEPOINT_EVENT);
        this.tracePointService = tracePointService;
    }

    @Override
    public EventAck handleEvent(ChannelInfo channelInfo, TracePointSnapshotEvent event, EventContext context) {
        probeEventListener.onProbeEvent(channelInfo, event, context);
        String tracePointId = event.getTracePointId();
        TracePointConfig tracePointConfig =
                tracePointService.getTracePoint(channelInfo.getWorkspaceId(), tracePointId);
        sendWebhookMessage(context.getRawMessage(), tracePointConfig);

        CompletableFuture<Boolean> completableFuture = tracePointService.
                checkExpireAndDecrementTracePointExpireCount(channelInfo.getWorkspaceId(), tracePointId);
        completableFuture.thenAccept(isExpire -> {
            if (isExpire) {
                if (tracePointConfig == null) {
                    return;
                }
                List<String> applications =
                        new ArrayList<>(applicationService.filterApplications(channelInfo.getWorkspaceId(),
                                tracePointConfig.getApplicationFilters()));
                RemoveTracePointRequest request = prepareRemoveTracePointRequest(event, tracePointId, applications);
                tracePointService.removeTracePoint(
                        channelInfo.getWorkspaceId(),
                        channelInfo.getUserId(),
                        tracePointId);
                communicator.sendRequestToApps(channelInfo, request, applications);
            }
        });
        return null;
    }

    private RemoveTracePointRequest prepareRemoveTracePointRequest(TracePointSnapshotEvent event, String tracePointId,
                                                                   List<String> applications) {
        RemoveTracePointRequest request = new RemoveTracePointRequest();
        request.setTracePointId(tracePointId);
        request.setApplications(applications);
        request.setId(UUIDUtils.generateId());
        request.setClient(event.getClient());
        request.setPersist(true);
        return request;
    }

    @Override
    protected void sendWebhookMessage(String messageRaw, TracePoint tracePoint) {
        try {
            if (!CollectionUtils.isEmpty(tracePoint.getWebhookIds())) {
                webhookMessageService.publishTracePointWebhookMessage(messageRaw, tracePoint);
            }
        } catch (Throwable t) {
            logger.error(t);
        }
    }

}
