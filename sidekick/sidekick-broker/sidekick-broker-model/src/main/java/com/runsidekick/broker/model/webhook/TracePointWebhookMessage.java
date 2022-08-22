package com.runsidekick.broker.model.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.runsidekick.broker.model.TracePoint;


/**
 * @author yasin.kalafat
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TracePointWebhookMessage extends WebhookMessage<TracePoint>  {

    public TracePointWebhookMessage(String messageRaw, TracePoint tracePoint) {
        super(messageRaw, tracePoint, WebhookMessageType.TRACEPOINT);
    }
}
