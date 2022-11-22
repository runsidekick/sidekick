package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.webhook.ErrorSnapshotWebhookMessage;
import com.runsidekick.broker.model.webhook.LogPointWebhookMessage;
import com.runsidekick.broker.model.webhook.TracePointWebhookMessage;
import com.runsidekick.broker.model.webhook.WebhookMessage;
import com.runsidekick.broker.service.WebhookMessageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author yasin.kalafat
 */
@Service
public class WebhookMessageServiceImpl implements WebhookMessageService {

    @Value("${amq.webhookQueueName:}")
    private String queueName;

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter jsonMessageConverter;

    public WebhookMessageServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMessageConverter = new Jackson2JsonMessageConverter();
        this.rabbitTemplate.setMessageConverter(this.jsonMessageConverter);
    }

    @Override
    public void publishTracePointWebhookMessage(String messageRaw, TracePoint tracePoint) {
        publishWebhookMessage(new TracePointWebhookMessage(messageRaw, tracePoint));
    }

    @Override
    public void publishLogPointWebhookMessage(String messageRaw, LogPoint logPoint) {
        publishWebhookMessage(new LogPointWebhookMessage(messageRaw, logPoint));
    }

    @Override
    public void publishErrorStackWebhookMessage(String messageRaw, String webhookId) {
        publishWebhookMessage(new ErrorSnapshotWebhookMessage(messageRaw, webhookId));
    }

    @Override
    public void publishWebhookMessage(WebhookMessage webhookMessage) {
        rabbitTemplate.convertAndSend(queueName, webhookMessage);
    }
}
