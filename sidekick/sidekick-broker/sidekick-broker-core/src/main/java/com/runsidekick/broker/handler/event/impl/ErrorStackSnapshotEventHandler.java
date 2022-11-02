package com.runsidekick.broker.handler.event.impl;

import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.model.BaseProbe;
import com.runsidekick.broker.model.event.EventAck;
import com.runsidekick.broker.model.event.impl.BaseEvent;
import com.runsidekick.broker.model.event.impl.ErrorStackSnapshotEvent;
import com.runsidekick.broker.proxy.ChannelInfo;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
public class ErrorStackSnapshotEventHandler extends BaseProbeEventHandler<BaseProbe, ErrorStackSnapshotEvent> {
    private static final String EVENT_NAME = "ErrorStackSnapshotEvent";


    public ErrorStackSnapshotEventHandler() {
        super(EVENT_NAME, ErrorStackSnapshotEvent.class);
    }

    @Override
    public EventAck handleEvent(ChannelInfo channelInfo, ErrorStackSnapshotEvent event, EventContext context) {
        context.setBroadcast(true);
        saveEventHistory(channelInfo, event, context.getRawMessage(), null);
        return null;
    }

    @Override
    protected void sendWebhookMessage(String messageRaw, BaseProbe probe) {
        //TODO common webhook endpoint will be set for error snapshots
    }

    @Override
    protected void saveEventHistory(ChannelInfo channelInfo, BaseEvent event, String messageRaw, BaseProbe probe) {
        try {
            eventHistoryService.addErrorSnapshotEventHistory(channelInfo.getWorkspaceId(),
                    (ErrorStackSnapshotEvent) event,
                    messageRaw);
        } catch (Throwable t) {
            logger.error(t);
        }
    }
}
