package com.runsidekick.broker.repository;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.TracePointConfig;

import java.util.Collection;
import java.util.List;

/**
 * @author ozge.lule
 */
public interface TracePointRepository {

    TracePointConfig getTracePoint(String workspaceId, String tracePointId);

    TracePoint getTracePointById(String id);

    void putTracePoint(String workspaceId, String userId, TracePointConfig tracePointConfig, boolean fromApi)
            throws Exception;

    void removeTracePoint(String workspaceId, String userId, String tracePointId);

    long removeTracePoints(String workspaceId, String userId, List<String> tracePointIds);

    void enableDisableTracePoint(String workspaceId, String userId, String tracePointId, boolean disable);

    void updateTracePoint(String workspaceId, String userId, String tracePointId, TracePoint tracePoint);

    List<TracePoint> listTracePoints(String workspaceId, String userId);

    Collection<TracePoint> queryTracePoints(String workspaceId, ApplicationFilter applicationFilter);

    List<TracePoint> listPredefinedTracePoints(String workspaceId, String userId);
}
