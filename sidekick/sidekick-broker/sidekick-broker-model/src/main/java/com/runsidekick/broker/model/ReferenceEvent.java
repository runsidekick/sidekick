package com.runsidekick.broker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yasin.kalafat
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceEvent {

    private String workspaceId;
    private String probeId;
    private ProbeType probeType;
    private ApplicationFilter applicationFilter;
    private String event;

}
