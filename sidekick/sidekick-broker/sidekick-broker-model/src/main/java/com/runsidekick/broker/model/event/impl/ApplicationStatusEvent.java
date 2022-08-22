package com.runsidekick.broker.model.event.impl;

import com.runsidekick.broker.model.Application;

/**
 * @author serkan.ozal
 */
public class ApplicationStatusEvent extends BaseEvent {

    private Application application;

    public ApplicationStatusEvent() {
        // By default send ACK
        setSendAck(true);
    }

    public ApplicationStatusEvent(Application application) {
        this.application = application;
        // By default send ACK
        setSendAck(true);
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    @Override
    public String toString() {
        return "ApplicationStatusEvent{" +
                "application=" + application +
                ", id='" + id + '\'' +
                '}';
    }

}
