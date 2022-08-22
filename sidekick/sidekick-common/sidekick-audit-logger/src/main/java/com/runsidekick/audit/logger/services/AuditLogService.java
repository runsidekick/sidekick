package com.runsidekick.audit.logger.services;

import com.runsidekick.audit.logger.dto.AuditLog;
import com.runsidekick.audit.logger.dto.AuditStack;
import com.runsidekick.audit.logger.dto.PrototypeScopedAuditStack;
import com.runsidekick.audit.logger.dto.RequestScopedAuditStack;
import com.runsidekick.audit.logger.providers.AuditLoggerProvider;
import com.runsidekick.audit.logger.providers.AuditLoggerProviderHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * @author yasin.kalafat
 */
@Slf4j
@Service
public class AuditLogService {

    AuditStack auditStack;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    AuditLoggerProvider provider;

    @Autowired
    AuditLoggerProviderHelper auditLoggerProviderHelper;

    @PostConstruct
    public void init() {
        if (provider.getStackScope().equals(WebApplicationContext.SCOPE_REQUEST)) {
            auditStack = applicationContext.getBean(RequestScopedAuditStack.class);
        } else {
            auditStack = applicationContext.getBean(PrototypeScopedAuditStack.class);
        }
    }

    public AuditLog createNewAuditLog(String action, String domain) {
        AuditLog auditLog = auditStack.push();
        auditLog.setAction(action);
        auditLog.setDomain(domain);

        auditLoggerProviderHelper.beforeProcess(auditLog);
        return auditLog;
    }

    public Optional<AuditLog> getCurrentAuditLog() {
        return auditStack.peek();
    }

    public void removeAuditLog(AuditLog auditLog) {
        auditStack.remove(auditLog);
    }
}
