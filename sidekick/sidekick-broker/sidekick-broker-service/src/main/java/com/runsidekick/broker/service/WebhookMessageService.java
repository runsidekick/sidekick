package com.runsidekick.broker.service;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.webhook.WebhookMessage;

/**
 * @author yasin.kalafat
 */
public interface WebhookMessageService {

    void publishTracePointWebhookMessage(String messageRaw, TracePoint tracePoint);

    void publishLogPointWebhookMessage(String messageRaw, LogPoint logPoint);

    void publishErrorStackWebhookMessage(String messageRaw, String webhookId);

    void publishWebhookMessage(WebhookMessage webhookMessage);

}
