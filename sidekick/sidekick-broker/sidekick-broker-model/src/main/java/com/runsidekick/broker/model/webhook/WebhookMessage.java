package com.runsidekick.broker.model.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.runsidekick.broker.model.BaseProbe;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * @author yasin.kalafat
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookMessage<P extends BaseProbe> {

    protected final String messageRaw;
    protected final Integer type;
    protected final List<String> webhookIds;

    protected WebhookMessage(String messageRaw, P probe, WebhookMessageType type) {
        this.messageRaw = messageRaw;
        this.webhookIds = probe.getWebhookIds();
        this.type = type.getValue();
    }
}
