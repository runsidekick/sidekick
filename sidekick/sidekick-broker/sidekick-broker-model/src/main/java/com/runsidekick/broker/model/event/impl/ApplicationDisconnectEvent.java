package com.runsidekick.broker.model.event.impl;

/**
 * @author ozge.lule
 */
public class ApplicationDisconnectEvent extends BaseEvent {

    @Override
    public String toString() {
        return "ApplicationDisconnectEvent{" +
                "id='" + id + '\'' +
                ", sendAck=" + sendAck +
                ", client='" + client + '\'' +
                ", time=" + time +
                ", hostName='" + hostName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationInstanceId='" + applicationInstanceId + '\'' +
                '}';
    }

}
