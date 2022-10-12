package com.runsidekick.broker.handler.event.impl;

import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.NotificationType;
import com.runsidekick.broker.model.event.EventAck;
import com.runsidekick.broker.model.event.impl.LogPointEvent;
import com.runsidekick.broker.model.request.impl.logpoint.RemoveLogPointRequest;
import com.runsidekick.broker.proxy.ChannelInfo;
import com.runsidekick.broker.service.LogPointService;
import io.thundra.swark.utils.UUIDUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yasin.kalafat
 */
@Component
public class LogPointEventHandler extends BaseProbeEventHandler<LogPoint, LogPointEvent> {
    private static final String EVENT_NAME = "LogPointEvent";

    private final LogPointService logPointService;

    public LogPointEventHandler(LogPointService logPointService) {
        super(EVENT_NAME, LogPointEvent.class, NotificationType.USER_DEBUGGER_FIRST_LOGPOINT_EVENT);
        this.logPointService = logPointService;
    }

    @Override
    public EventAck handleEvent(ChannelInfo channelInfo, LogPointEvent event, EventContext context) {
        probeEventListener.onProbeEvent(channelInfo, event, context);
        String logPointId = event.getLogPointId();
        LogPointConfig logPointConfig =
                logPointService.getLogPoint(channelInfo.getWorkspaceId(), logPointId);
        sendWebhookMessage(context.getRawMessage(), logPointConfig);

        CompletableFuture<Boolean> completableFuture = logPointService.
                checkExpireAndDecrementLogPointExpireCount(channelInfo.getWorkspaceId(), logPointId);
        completableFuture.thenAccept(isExpire -> {
            if (isExpire) {
                if (logPointConfig == null) {
                    return;
                }
                if (!logPointConfig.hasTag()) {
                    List<String> applications =
                            new ArrayList<>(applicationService.filterApplications(channelInfo.getWorkspaceId(),
                                    logPointConfig.getApplicationFilters()));
                    RemoveLogPointRequest request = prepareRemoveLogPointRequest(event, logPointId, applications);
                    logPointService.removeLogPoint(
                            channelInfo.getWorkspaceId(),
                            channelInfo.getUserId(),
                            logPointId);
                    communicator.sendRequestToApps(channelInfo, request, applications);
                }
            }
        });
        return null;
    }

    private RemoveLogPointRequest prepareRemoveLogPointRequest(LogPointEvent event, String logPointId,
                                                               List<String> applications) {
        RemoveLogPointRequest request = new RemoveLogPointRequest();
        request.setLogPointId(logPointId);
        request.setApplications(applications);
        request.setId(UUIDUtils.generateId());
        request.setClient(event.getClient());
        request.setPersist(true);
        return request;
    }

    @Override
    protected void sendWebhookMessage(String messageRaw, LogPoint logPoint) {
        try {
            if (!CollectionUtils.isEmpty(logPoint.getWebhookIds())) {
                webhookMessageService.publishLogPointWebhookMessage(messageRaw, logPoint);
            }
        } catch (Throwable t) {
            logger.error(t);
        }
    }
}
