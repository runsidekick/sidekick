package com.runsidekick.broker;

import com.runsidekick.service.PhoneHomeMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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
public class SidekickBrokerApplication implements CommandLineRunner {

    @Value("${phonehome.enabled:true}")
    private boolean phoneHomeEnabled;

    @Autowired
    private PhoneHomeMetricService phoneHomeMetricService;

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

    @Override
    public void run(String...args) throws Exception {
        if (phoneHomeEnabled) {
            long startTime = System.currentTimeMillis();
            phoneHomeMetricService.sendServerUpEvent(startTime);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                long finishTime = System.currentTimeMillis();
                phoneHomeMetricService.sendServerDownEvent(startTime, finishTime);
            }));
        }
    }

}
