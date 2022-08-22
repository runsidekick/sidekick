package com.runsidekick.audit.logger.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.EmptyStackException;
import java.util.Optional;
import java.util.Stack;

/**
 * @author yasin.kalafat
 */
@Slf4j
public abstract class AuditStack {

    Stack<AuditLog> auditLogStack = new Stack();

    public AuditLog push() {
        return generateNewAuditLog();
    }

    public Optional<AuditLog> peek() {
        try {
            return Optional.of(auditLogStack.peek());
        } catch (EmptyStackException e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public void remove(AuditLog auditLog) {
        auditLogStack.remove(auditLog);
    }

    private AuditLog generateNewAuditLog() {
        AuditLog auditLog = new AuditLog();
        auditLogStack.push(auditLog);
        return auditLog;
    }
}
