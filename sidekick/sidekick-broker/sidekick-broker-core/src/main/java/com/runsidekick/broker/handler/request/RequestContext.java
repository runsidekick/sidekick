package com.runsidekick.broker.handler.request;

import org.json.JSONObject;

/**
 * @author serkan.ozal
 */
public class RequestContext {

    private final JSONObject requestMessage;
    private boolean requestUpdated;

    public RequestContext(JSONObject requestMessage) {
        this.requestMessage = requestMessage;
    }

    public JSONObject getRequestMessage() {
        return requestMessage;
    }

    public void putToRequest(String propName, boolean propValue) {
        requestMessage.put(propName, propValue);
        requestUpdated = true;
    }

    public void putToRequest(String propName, int propValue) {
        requestMessage.put(propName, propValue);
        requestUpdated = true;
    }

    public void putToRequest(String propName, long propValue) {
        requestMessage.put(propName, propValue);
        requestUpdated = true;
    }

    public void putToRequest(String propName, double propValue) {
        requestMessage.put(propName, propValue);
        requestUpdated = true;
    }

    public void putToRequest(String propName, String propValue) {
        requestMessage.put(propName, propValue);
        requestUpdated = true;
    }

    public void removeFromRequest(String propName) {
        requestMessage.remove(propName);
        requestUpdated = true;
    }

    public boolean isRequestUpdated() {
        return requestUpdated;
    }

    public void setRequestUpdated(boolean requestUpdated) {
        this.requestUpdated = requestUpdated;
    }

}
