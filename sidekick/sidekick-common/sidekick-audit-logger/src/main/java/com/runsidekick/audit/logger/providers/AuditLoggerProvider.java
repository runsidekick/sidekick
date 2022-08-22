package com.runsidekick.audit.logger.providers;

import org.slf4j.Logger;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author yasin.kalafat
 */
public interface AuditLoggerProvider {

    String getUserId();

    String getUserEmail();

    String getAccountId();

    String getIpAddress();

    String getApplicationName();

    Logger getLogger();

    default boolean hasUserInfo() {
        return true;
    }

    default String getStackScope() {
        return WebApplicationContext.SCOPE_REQUEST;
    }

}
