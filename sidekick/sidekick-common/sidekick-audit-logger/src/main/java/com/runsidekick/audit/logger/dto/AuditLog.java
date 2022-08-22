package com.runsidekick.audit.logger.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Data
@Component
@Scope(value = "prototype")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLog implements Serializable {

    private String action;

    private String domain;

    private String time = LocalDateTime.now().toString();

    private String email;

    private String userId;

    private String accountId;

    private String ip;

    private String result;

    private String traceId;

    private String applicationName;

    private String errorType;

    private String errorMessage;

    private Map<String, Object> fields = new HashMap<>();

    public void addAuditLogField(String name, Object value) {
        this.fields.put(name, value);
    }
}
