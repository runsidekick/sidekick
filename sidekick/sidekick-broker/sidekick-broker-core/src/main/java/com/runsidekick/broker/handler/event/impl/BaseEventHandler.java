package com.runsidekick.broker.handler.event.impl;

import com.runsidekick.broker.handler.event.EventHandler;
import com.runsidekick.broker.model.event.Event;
import com.runsidekick.broker.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author serkan.ozal
 */
public abstract class BaseEventHandler<E extends Event>
        implements EventHandler<E> {

    protected final String eventName;
    protected final Class<E> eventClass;

    @Autowired
    protected ApplicationService applicationService;

    protected BaseEventHandler(String eventName, Class<E> eventClass) {
        this.eventName = eventName;
        this.eventClass = eventClass;
    }

    public String getEventName() {
        return eventName;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

}
