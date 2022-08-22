package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.webhook.LogPointWebhookMessage;
import com.runsidekick.broker.model.webhook.TracePointWebhookMessage;
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
        rabbitTemplate.convertAndSend(queueName, new TracePointWebhookMessage(messageRaw, tracePoint));
    }

    @Override
    public void publishLogPointWebhookMessage(String messageRaw, LogPoint logPoint) {
        rabbitTemplate.convertAndSend(queueName, new LogPointWebhookMessage(messageRaw, logPoint));
    }
}
