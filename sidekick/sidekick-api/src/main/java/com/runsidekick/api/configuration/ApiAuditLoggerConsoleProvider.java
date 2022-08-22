package com.runsidekick.api.configuration;

import com.runsidekick.audit.logger.providers.AuditLoggerProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.runsidekick.api.util.ClientUtils.getClientIpAddresses;

/**
 * @author yasin.kalafat
 */
@Slf4j
@Component
public class ApiAuditLoggerConsoleProvider implements AuditLoggerProvider {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    HttpServletRequest httpRequest;

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
        List<String> clientIpAddresses = getClientIpAddresses(httpRequest, -1);
        return clientIpAddresses.get(clientIpAddresses.size() - 1);
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger("AUDIT_LOGGER");
    }
}
