package com.runsidekick.broker.handler.event.impl;

import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.model.event.EventAck;
import com.runsidekick.broker.model.event.impl.ErrorStackSnapshotEvent;
import com.runsidekick.broker.proxy.ChannelInfo;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
public class ErrorStackSnapshotEventHandler extends BaseEventHandler<ErrorStackSnapshotEvent> {
    private static final String EVENT_NAME = "ErrorStackSnapshotEvent";


    public ErrorStackSnapshotEventHandler() {
        super(EVENT_NAME, ErrorStackSnapshotEvent.class);
    }

    @Override
    public EventAck handleEvent(ChannelInfo channelInfo, ErrorStackSnapshotEvent event, EventContext context) {
        context.setBroadcast(true);
        return null;
    }

}
