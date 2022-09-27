package com.runsidekick.service;

import com.runsidekick.model.ServerStatistics;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ServerStatisticsService {

    void increaseApplicationInstanceCount(String workspaceId);

    void increaseTracePointCount(String workspaceId);

    void increaseLogPointCount(String workspaceId);

    List<ServerStatistics> getAllServerStatistics();

    ServerStatistics getServerStatistics(String workspaceId);
}
