package com.runsidekick.broker.model.event;

/**
 * @author serkan.ozal
 */
public interface Event {

    default String getType() {
        return "Event";
    }

    String getName();

    String getId();

    boolean isSendAck();

    String getClient();

    long getTime();

    String getHostName();

    String getApplicationName();

    String getApplicationInstanceId();

}
