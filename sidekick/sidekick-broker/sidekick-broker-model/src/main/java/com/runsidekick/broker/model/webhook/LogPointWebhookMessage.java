package com.runsidekick.broker.model.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.runsidekick.broker.model.LogPoint;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


/**
 * @author yasin.kalafat
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogPointWebhookMessage extends WebhookMessage<LogPoint> {

    private final Map<String, Object> extraFields = new HashMap<>();

    public LogPointWebhookMessage(String messageRaw, LogPoint logPoint) {
        super(messageRaw, logPoint, WebhookMessageType.LOGPOINT);
        this.extraFields.put("logLevel", logPoint.getLogLevel());
    }
}
