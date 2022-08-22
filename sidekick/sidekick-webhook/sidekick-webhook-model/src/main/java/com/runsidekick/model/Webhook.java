package com.runsidekick.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author yasin.kalafat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {
    private String id;
    @NotNull
    private String workspaceId;
    @NotNull
    private String name;
    @NotNull
    private WebhookType type;
    private String config;
    private boolean disabled;
    private String lastErrorReason;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastErrorDate;
}
