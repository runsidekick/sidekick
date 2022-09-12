package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.LogPointConfig;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.repository.LogPointExpireCountRepository;
import com.runsidekick.broker.repository.LogPointRepository;
import com.runsidekick.broker.service.LogPointService;
import com.runsidekick.broker.service.ReferenceEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author yasin.kalafat
 */
@RequiredArgsConstructor
@Service
public class LogPointServiceImpl implements LogPointService {

    private final LogPointRepository logPointRepository;

    private final LogPointExpireCountRepository logPointExpireCountRepository;

    private final ReferenceEventService referenceEventService;

    @Override
    @Cacheable(cacheNames = "LogPoint", key = "#workspaceId + '_' + #logPointId")
    public LogPointConfig getLogPoint(String workspaceId, String logPointId) {
        return logPointRepository.getLogPoint(workspaceId, logPointId);
    }

    @Override
    @CacheEvict(cacheNames = "LogPoint", key = "#workspaceId + '_' + #logPointConfig.id")
    public void putLogPoint(String workspaceId, String userId, LogPointConfig logPointConfig, boolean fromApi)
            throws Exception {
        logPointRepository.putLogPoint(workspaceId, userId, logPointConfig, fromApi);
        if (!logPointConfig.isPredefined()) {
            logPointExpireCountRepository.putLogPointExpireCount(workspaceId, logPointConfig.getId(),
                    logPointConfig.getExpireCount(), logPointConfig.getExpireSecs());
        }
    }

    @Override
    @CacheEvict(cacheNames = "LogPoint", key = "#workspaceId + '_' + #logPointId")
    public void removeLogPoint(String workspaceId, String userId, String logPointId) {
        logPointRepository.removeLogPoint(workspaceId, userId, logPointId);
        logPointExpireCountRepository.removeLogPointExpireCount(workspaceId, logPointId);
        referenceEventService.removeReferenceEvent(workspaceId, logPointId, ProbeType.LOGPOINT);
    }

    @Override
    public long removeLogPoints(String workspaceId, String userId, List<String> logPointIds) {
        long deletedCount = logPointRepository.removeLogPoints(workspaceId, userId, logPointIds);
        logPointExpireCountRepository.removeLogPointsExpireCount(workspaceId, logPointIds);
        referenceEventService.removeReferenceEvents(workspaceId, logPointIds, ProbeType.LOGPOINT);
        return deletedCount;
    }

    @Override
    @CacheEvict(cacheNames = "LogPoint", key = "#workspaceId + '_' + #logPointId")
    public void enableDisableLogPoint(String workspaceId, String userId, String logPointId, boolean disable) {
        logPointRepository.enableDisableLogPoint(workspaceId, userId, logPointId, disable);
    }

    @Override
    @CacheEvict(cacheNames = "LogPoint", key = "#workspaceId + '_' + #logPointId")
    public void updateLogPoint(String workspaceId, String userId, String logPointId, LogPoint logPoint) {
        logPointRepository.updateLogPoint(workspaceId, userId, logPointId, logPoint);
        if (logPoint.isPredefined()) {
            logPointExpireCountRepository.removeLogPointExpireCount(workspaceId, logPointId);
        } else {
            logPointExpireCountRepository.putLogPointExpireCount(
                    workspaceId, logPointId, logPoint.getExpireCount(), logPoint.getExpireSecs());
            referenceEventService.removeReferenceEvent(workspaceId, logPointId, ProbeType.LOGPOINT);
        }
    }

    @Override
    public List<LogPoint> listLogPoints(String workspaceId, String userId) {
        return logPointRepository.listLogPoints(workspaceId, userId);
    }

    @Override
    public Collection<LogPoint> queryLogPoints(String workspaceId, ApplicationFilter filter) {
        return logPointRepository.queryLogPoints(workspaceId, filter);
    }

    @Override
    public CompletableFuture<Boolean> checkExpireAndDecrementLogPointExpireCount(
            String workspaceId, String logPointId) {
        return logPointExpireCountRepository.checkExpireAndDecrementLogPointExpireCount(workspaceId, logPointId);
    }

    @Override
    public List<LogPoint> listPredefinedLogPoints(String workspaceId, String userId) {
        return logPointRepository.listPredefinedLogPoints(workspaceId, userId);
    }

    @Override
    public LogPoint queryLogPoint(String workspaceId, String logPointId, ApplicationFilter applicationFilter) {
        return logPointRepository.queryLogPoint(workspaceId, logPointId, applicationFilter);
    }

}
