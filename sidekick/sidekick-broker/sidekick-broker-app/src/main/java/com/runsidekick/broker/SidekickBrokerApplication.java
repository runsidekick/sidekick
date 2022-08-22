package com.runsidekick.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author serkan.ozal
 */
@SpringBootApplication(
        scanBasePackages = {"io.thundra", "com.runsidekick"}
)
@RestController
@EnableScheduling
public class SidekickBrokerApplication {

    @RequestMapping("/")
    public String home() {
        return "Hello from Sidekick Broker";
    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping() {
        return "pong";
    }

    public static void main(String[] args) {
        SpringApplication.run(SidekickBrokerApplication.class, args);
    }

}
