package com.runsidekick.broker.model.webhook;

/**
 * @author yasin.kalafat
 */
public enum WebhookMessageType {
    TRACEPOINT(0), LOGPOINT(1);

    private int value;

    WebhookMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
