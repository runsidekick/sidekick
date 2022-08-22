package com.runsidekick.audit.logger.dto;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author yasin.kalafat
 */
@Component
@Scope("prototype")
public class PrototypeScopedAuditStack extends AuditStack {
}
