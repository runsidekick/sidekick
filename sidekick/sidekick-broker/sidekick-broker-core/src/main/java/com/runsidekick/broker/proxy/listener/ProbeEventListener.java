package com.runsidekick.broker.proxy.listener;

import com.runsidekick.broker.handler.event.EventContext;
import com.runsidekick.broker.model.event.impl.BaseEvent;
import com.runsidekick.broker.proxy.ChannelInfo;

/**
 * @author yasin.kalafat
 */
public interface ProbeEventListener {

    default void onProbeEvent(ChannelInfo channelInfo, BaseEvent event, EventContext context) {
    }
}
