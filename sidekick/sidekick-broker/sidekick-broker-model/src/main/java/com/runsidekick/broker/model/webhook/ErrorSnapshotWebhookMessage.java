package com.runsidekick.broker.model.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.runsidekick.broker.model.LogPoint;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * @author yasin.kalafat
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorSnapshotWebhookMessage extends WebhookMessage<LogPoint> {

    private final Map<String, Object> extraFields = new HashMap<>();

    public ErrorSnapshotWebhookMessage(String messageRaw, String webhookId) {
        super(messageRaw, WebhookMessageType.ERROR_SNAPSHOT, Collections.singletonList(webhookId));
    }
}
