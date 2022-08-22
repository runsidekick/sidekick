package com.runsidekick.broker.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author serkan.ozal
 */
@Configuration
public class SerializationConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().
                        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
                        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}
