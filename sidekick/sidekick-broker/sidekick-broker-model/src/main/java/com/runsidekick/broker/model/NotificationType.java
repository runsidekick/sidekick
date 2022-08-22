package com.runsidekick.broker.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize(using = NotificationType.NotificationTypeSerializer.class)
@JsonDeserialize(using = NotificationType.NotificationTypeDeSerializer.class)
public enum NotificationType {

    USER_INVITED(1, "USER_INVITED"),
    USER_DEBUGGER_FIRST_TRACEPOINT_EVENT(2, "USER_DEBUGGER_FIRST_TRACEPOINT_EVENT"),
    USER_DEBUGGER_ONBOARD_PASSED_EVENT(3, "USER_DEBUGGER_ONBOARD_PASSED_EVENT"),
    USER_DEBUGGER_FIRST_LOGPOINT_EVENT(4, "USER_DEBUGGER_FIRST_LOGPOINT_EVENT");

    private int code;
    private String text;

    NotificationType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static NotificationType getDefault() {
        return null;
    }

    public static NotificationType getTypeByCode(int code) {
        for (NotificationType status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

    public static NotificationType getTypeByText(String text) {
        for (NotificationType status : values()) {
            if (status.getText().equals(text)) {
                return status;
            }
        }
        return null;
    }

    public static NotificationType getTypeValue(String text) {
        for (NotificationType status : values()) {
            if (status.toString().equals(text)) {
                return status;
            }
        }
        return null;
    }

    public static class NotificationTypeSerializer extends JsonSerializer<NotificationType> {
        @Override
        public void serialize(NotificationType type, JsonGenerator jgen,
                              SerializerProvider provider) throws IOException {
            jgen.writeString(type.getText());
        }
    }

    public static class NotificationTypeDeSerializer extends JsonDeserializer<NotificationType> {
        @Override
        public NotificationType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return NotificationType.getTypeByText(parser.getText());
        }
    }

}
