package com.runsidekick.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author yasin.kalafat
 */
//CHECKSTYLE:OFF
@SpringBootApplication(
        scanBasePackages = {"io.thundra", "com.runsidekick"}
)
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class SidekickApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SidekickApiApplication.class, args);
    }
}
//CHECKSTYLE:ON