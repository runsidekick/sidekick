package com.runsidekick.broker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * @author serkan.ozal
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Session {

    private boolean app;
    private String ip;
    private Map<String, String> tags;

    public Session() {
    }

    public Session(boolean app, String ip, Map<String, String> tags) {
        this.app = app;
        this.ip = ip;
        this.tags = tags;
    }

    public boolean isApp() {
        return app;
    }

    public void setApp(boolean app) {
        this.app = app;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "SessionPeerStatus{" +
                "app=" + app +
                ", ip='" + ip + '\'' +
                ", tags=" + tags +
                '}';
    }

}
