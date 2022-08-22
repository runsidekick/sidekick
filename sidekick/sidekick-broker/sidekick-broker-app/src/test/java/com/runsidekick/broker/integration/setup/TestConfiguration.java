package com.runsidekick.broker.integration.setup;

import com.runsidekick.broker.proxy.TestBrokerListener;
import com.runsidekick.broker.proxy.listener.BrokerListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author serkan.ozal
 */
@Configuration
public class TestConfiguration {

    @Bean
    public ContainerContextHolder createContainerContextHolder() {
        return new ContainerContextHolder();
    }

    @Bean
    public BrokerListener createBrokerListener() {
        return new TestBrokerListener();
    }

}

