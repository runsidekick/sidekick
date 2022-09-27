package com.runsidekick.repository;

import com.runsidekick.model.ServerStatistics;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ServerStatisticsRepository {

    boolean add(String workspaceId);

    void increaseApplicationInstanceCount(String workspaceId);

    void increaseTracePointCount(String workspaceId);

    void increaseLogPointCount(String workspaceId);

    ServerStatistics getServerStatistics(String workspaceId);

    List<ServerStatistics> getAllServerStatistics();
}
