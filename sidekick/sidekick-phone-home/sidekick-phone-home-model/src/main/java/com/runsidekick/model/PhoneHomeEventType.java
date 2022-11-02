package com.runsidekick.model;

/**
 * @author yasin.kalafat
 */
public enum PhoneHomeEventType {
    SERVER_UP("SERVER_UP"), SERVER_DOWN("SERVER_DOWN"), STATISTICS("STATISTICS");

    private String text;

    PhoneHomeEventType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
