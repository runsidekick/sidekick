package com.runsidekick.audit.logger.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runsidekick.audit.logger.dto.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Slf4j
@Component
public class AuditLoggerProviderHelper {

    @Autowired
    AuditLoggerProvider provider;

    @Autowired
    ObjectMapper mapper;

    public void beforeProcess(AuditLog auditLog) {
        if (provider != null) {
            if (provider.hasUserInfo()) {
                auditLog.setIp(provider.getIpAddress());
                auditLog.setAccountId(provider.getAccountId());
                auditLog.setEmail(provider.getUserEmail());
                auditLog.setUserId(provider.getUserId());
            }
            auditLog.setApplicationName(provider.getApplicationName());
        }
    }

    public void afterProcess(AuditLog auditLog) {
        try {
            String auditLogString = mapper.writeValueAsString(auditLog);
            Logger auditLogger = provider.getLogger();
            if (auditLogger == null) {
                log.warn("Audit Log logger was not set, please set logger for view audit logs");
            } else {
                auditLogger.info(auditLogString);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
    }

}
