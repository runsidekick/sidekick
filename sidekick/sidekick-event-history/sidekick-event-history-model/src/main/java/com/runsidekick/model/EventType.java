package com.runsidekick.model;

/**
 * @author yasin.kalafat
 */
public enum EventType {
    TRACEPOINT,
    LOGPOINT,
    ERRORSNAPSHOT;

    public static EventType toEventType(String type) {
        for (EventType eventType : EventType.values()) {
            if (eventType.name().equalsIgnoreCase(type)) {
                return eventType;
            }
        }
        return null;
    }
}
