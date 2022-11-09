package com.runsidekick.broker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author yasin.kalafat
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationConfig {

    private String id;
    private String workspaceId;
    private ApplicationFilter applicationFilter;
    private Map<String, Object> config;
    private boolean detached;

}
