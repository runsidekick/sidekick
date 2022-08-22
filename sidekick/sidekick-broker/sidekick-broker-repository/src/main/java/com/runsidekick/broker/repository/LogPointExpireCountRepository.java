package com.runsidekick.broker.repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yasin.kalafat
 */
public interface LogPointExpireCountRepository {

    void putLogPointExpireCount(String workspaceId, String logPointId, Integer expireCount, Integer expireSecs);

    boolean removeLogPointExpireCount(String workspaceId, String logPointId);

    boolean removeLogPointsExpireCount(String workspaceId, List<String> logPointIds);

    CompletableFuture<Boolean> checkExpireAndDecrementLogPointExpireCount(String workspaceId, String logPointId);
}
