package com.runsidekick.broker.model.event;

/**
 * @author serkan.ozal
 */
public class EventAck {

    private String eventId;
    private boolean erroneous;
    private int errorCode;
    private String errorMessage;

    public EventAck() {
    }

    public EventAck(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public boolean isErroneous() {
        return erroneous;
    }

    public void setErroneous(boolean erroneous) {
        this.erroneous = erroneous;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "EventAck{" +
                "eventId='" + eventId + '\'' +
                ", erroneous=" + erroneous +
                ", errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}
