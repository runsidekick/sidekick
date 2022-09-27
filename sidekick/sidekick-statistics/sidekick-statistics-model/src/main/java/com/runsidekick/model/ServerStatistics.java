package com.runsidekick.model;

import lombok.Data;

/**
 * @author yasin.kalafat
 */
@Data
public class ServerStatistics {

    private String workspaceId;
    private int applicationInstanceCount;
    private int tracepointCount;
    private int logpointCount;

}
