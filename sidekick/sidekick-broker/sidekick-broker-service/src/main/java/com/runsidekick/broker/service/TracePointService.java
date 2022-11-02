package com.runsidekick.broker.service;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.TracePointConfig;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author ozge.lule
 */
public interface TracePointService {

    TracePointConfig getTracePoint(String workspaceId, String tracePointId);

    void putTracePoint(String workspaceId, String userId, TracePointConfig tracePointConfig, boolean fromApi)
            throws Exception;

    void removeTracePoint(String workspaceId, String userId, String tracePointId);

    long removeTracePoints(String workspaceId, String userId, List<String> tracePointIds);

    void enableDisableTracePoint(String workspaceId, String userId, String tracePointId, boolean disable);

    void enableDisableTracePoints(String workspaceId, List<String> tracePointIds, boolean disable);

    void updateTracePoint(String workspaceId, String userId, String tracePointId, TracePoint tp);

    List<TracePoint> listTracePoints(String workspaceId, String userId);

    Collection<TracePoint> queryTracePoints(String workspaceId, ApplicationFilter filter);

    CompletableFuture<Boolean> checkExpireAndDecrementTracePointExpireCount(String workspaceId, String tracePointId);

    List<TracePoint> listPredefinedTracePoints(String workspaceId, String userId);

    TracePoint queryTracePoint(String workspaceId, String tracePointId, ApplicationFilter filter);

    List<TracePointConfig> queryTracePointsByTag(String workspaceId, String tag);
}
