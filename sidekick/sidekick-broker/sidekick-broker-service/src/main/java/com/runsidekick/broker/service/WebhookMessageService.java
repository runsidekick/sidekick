package com.runsidekick.broker.service;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;

/**
 * @author yasin.kalafat
 */
public interface WebhookMessageService {

    void publishTracePointWebhookMessage(String messageRaw, TracePoint tracePoint);

    void publishLogPointWebhookMessage(String messageRaw, LogPoint logPoint);

}
