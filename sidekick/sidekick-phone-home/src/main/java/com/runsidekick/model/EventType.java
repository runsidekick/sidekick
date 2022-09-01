package com.runsidekick.model;

/**
 * @author yasin.kalafat
 */
public enum EventType {
    SERVER_UP("SERVER_UP");

    private String text;

    EventType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
