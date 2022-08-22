package com.runsidekick.broker.repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author tolgatakir
 */
public interface TracePointExpireCountRepository {

    void putTracePointExpireCount(String workspaceId, String tracePointId, Integer expireCount, Integer expireSecs);

    boolean removeTracePointExpireCount(String workspaceId, String tracePointId);

    boolean removeTracePointsExpireCount(String workspaceId, List<String> tracePointIds);

    CompletableFuture<Boolean> checkExpireAndDecrementTracePointExpireCount(String workspaceId, String tracePointId);
}
