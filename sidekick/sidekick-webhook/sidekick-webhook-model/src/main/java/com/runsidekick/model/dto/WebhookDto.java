package com.runsidekick.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.runsidekick.model.Webhook;
import com.runsidekick.model.WebhookType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author yasin.kalafat
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDto {

    private final String config = "{}";

    private String id;
    private String workspaceId;
    private String name;
    private WebhookType type;
    private boolean disabled;
    private String lastErrorReason;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastErrorDate;

    public static WebhookDto convert(Webhook webhook) {
        if (webhook == null) {
            return null;
        }
        return new WebhookDto(webhook.getId(), webhook.getWorkspaceId(), webhook.getName(),
                webhook.getType(), webhook.isDisabled(), webhook.getLastErrorReason(), webhook.getLastErrorDate());
    }
}
