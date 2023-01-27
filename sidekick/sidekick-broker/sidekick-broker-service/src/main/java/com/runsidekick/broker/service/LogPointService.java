package com.runsidekick.broker.service;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.LogPointConfig;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yasin.kalafat
 */
public interface LogPointService {

    LogPointConfig getLogPoint(String workspaceId, String logPointId);

    void putLogPoint(String workspaceId, String userId, LogPointConfig logPointConfig, boolean fromApi)
            throws Exception;

    void removeLogPoint(String workspaceId, String userId, String logPointId);

    long removeLogPoints(String workspaceId, String userId, List<String> logPointIds);

    void enableDisableLogPoint(String workspaceId, String userId, String logPointId, boolean disable);

    void enableDisableLogPoints(String workspaceId, List<String> logPointIds, boolean disable);

    void updateLogPoint(String workspaceId, String userId, String logPointId, LogPoint tp);

    List<LogPoint> listLogPoints(String workspaceId, String userId);

    Collection<LogPoint> queryLogPoints(String workspaceId, ApplicationFilter filter);

    CompletableFuture<Boolean> checkExpireAndDecrementLogPointExpireCount(String workspaceId, String logPointId);

    List<LogPoint> listPredefinedLogPoints(String workspaceId, String userId);

    LogPoint queryLogPoint(String workspaceId, String logPointId, ApplicationFilter applicationFilter);

    List<LogPointConfig> queryLogPointsByTag(String workspaceId, String tag);

    void deleteTag(String workspaceId, String tag);
}
