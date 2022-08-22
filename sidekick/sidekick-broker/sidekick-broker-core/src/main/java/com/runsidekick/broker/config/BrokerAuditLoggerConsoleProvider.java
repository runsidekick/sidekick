package com.runsidekick.broker.config;

import com.runsidekick.audit.logger.providers.AuditLoggerProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BrokerAuditLoggerConsoleProvider implements AuditLoggerProvider {

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public String getUserId() {
        return null;
    }

    @Override
    public String getUserEmail() {
        return null;
    }

    @Override
    public String getAccountId() {
        return null;
    }

    @Override
    public String getIpAddress() {
        return null;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger("AUDIT_LOGGER");
    }

    @Override
    public boolean hasUserInfo() {
        return false;
    }

    @Override
    public String getStackScope() {
        return "prototype";
    }


}
