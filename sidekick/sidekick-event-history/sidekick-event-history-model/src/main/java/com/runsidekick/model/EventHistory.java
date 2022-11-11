package com.runsidekick.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.runsidekick.broker.model.ApplicationFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yasin.kalafat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHistory {
    private String id;
    private String workspaceId;
    private EventType type;
    private ApplicationFilter applicationFilter;
    private String fileName;
    private int lineNo;
    private String client;
    private String eventData;
    private String probeName;
    private List<String> probeTags;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
}
