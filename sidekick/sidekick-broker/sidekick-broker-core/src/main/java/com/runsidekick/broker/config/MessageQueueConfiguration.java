package com.runsidekick.broker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yasin.kalafat
 */
@Configuration
public class MessageQueueConfiguration {

    @Value("${amq.webhookQueueName:}")
    private String queueName;
    private static final boolean DURABLE = true;

    @Bean
    public Queue webhookQueue() {
        return new Queue(queueName, DURABLE);
    }
}
