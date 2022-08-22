package com.runsidekick.broker.model.event.impl;

import com.runsidekick.broker.model.event.Event;

/**
 * @author serkan.ozal
 */
public abstract class BaseEvent implements Event {

    protected String id;
    protected boolean sendAck;
    protected String client;
    protected long time;
    protected String hostName;
    protected String applicationName;
    protected String applicationInstanceId;

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isSendAck() {
        return sendAck;
    }

    public void setSendAck(boolean sendAck) {
        this.sendAck = sendAck;
    }

    @Override
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getApplicationInstanceId() {
        return applicationInstanceId;
    }

    public void setApplicationInstanceId(String applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
    }

}
