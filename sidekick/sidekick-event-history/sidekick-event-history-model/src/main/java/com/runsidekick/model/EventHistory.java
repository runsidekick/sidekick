package com.runsidekick.model;

import com.runsidekick.broker.model.ApplicationFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
