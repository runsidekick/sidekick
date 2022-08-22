package com.runsidekick.broker.handler.event;

import com.runsidekick.broker.model.event.Event;
import com.runsidekick.broker.model.event.EventAck;
import com.runsidekick.broker.proxy.ChannelInfo;

/**
 * @author serkan.ozal
 */
public interface EventHandler<E extends Event> {

    String getEventName();

    Class<E> getEventClass();

    EventAck handleEvent(ChannelInfo channelInfo, E event, EventContext context);

}
