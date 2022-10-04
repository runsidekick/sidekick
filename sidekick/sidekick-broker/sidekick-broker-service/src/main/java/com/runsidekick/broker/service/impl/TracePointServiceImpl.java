package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.TracePointConfig;
import com.runsidekick.broker.repository.TracePointExpireCountRepository;
import com.runsidekick.broker.repository.TracePointRepository;
import com.runsidekick.broker.service.ReferenceEventService;
import com.runsidekick.broker.service.TracePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author ozge.lule
 */
@RequiredArgsConstructor
@Service
public class TracePointServiceImpl implements TracePointService {

    private final TracePointRepository tracePointRepository;

    private final TracePointExpireCountRepository tracePointExpireCountRepository;

    private final ReferenceEventService referenceEventService;

    @Override
    @Cacheable(cacheNames = "TracePoint", key = "#workspaceId + '_' + #tracePointId")
    public TracePointConfig getTracePoint(String workspaceId, String tracePointId) {
        return tracePointRepository.getTracePoint(workspaceId, tracePointId);
    }

    @Override
    @CacheEvict(cacheNames = "TracePoint", key = "#workspaceId + '_' + #tracePointConfig.id")
    public void putTracePoint(String workspaceId, String userId, TracePointConfig tracePointConfig, boolean fromApi)
            throws Exception {
        tracePointRepository.putTracePoint(workspaceId, userId, tracePointConfig, fromApi);
        if (!tracePointConfig.isPredefined()) {
            tracePointExpireCountRepository.putTracePointExpireCount(workspaceId, tracePointConfig.getId(),
                    tracePointConfig.getExpireCount(), tracePointConfig.getExpireSecs());
        }
    }

    @Override
    @CacheEvict(cacheNames = "TracePoint", key = "#workspaceId + '_' + #tracePointId")
    public void removeTracePoint(String workspaceId, String userId, String tracePointId) {
        tracePointRepository.removeTracePoint(workspaceId, userId, tracePointId);
        tracePointExpireCountRepository.removeTracePointExpireCount(workspaceId, tracePointId);
        referenceEventService.removeReferenceEvent(workspaceId, tracePointId, ProbeType.TRACEPOINT);
    }

    @Override
    public long removeTracePoints(String workspaceId, String userId, List<String> tracePointIds) {
        long deletedCount = tracePointRepository.removeTracePoints(workspaceId, userId, tracePointIds);
        tracePointExpireCountRepository.removeTracePointsExpireCount(workspaceId, tracePointIds);
        referenceEventService.removeReferenceEvents(workspaceId, tracePointIds, ProbeType.TRACEPOINT);
        return deletedCount;
    }

    @Override
    @CacheEvict(cacheNames = "TracePoint", key = "#workspaceId + '_' + #tracePointId")
    public void enableDisableTracePoint(String workspaceId, String userId, String tracePointId, boolean disable) {
        tracePointRepository.enableDisableTracePoint(workspaceId, userId, tracePointId, disable);
    }

    @Override
    public void enableDisableTracePoints(String workspaceId, List<String> tracePointIds, boolean disable) {
        tracePointRepository.enableDisableTracePoints(workspaceId, tracePointIds, disable);
    }

    @Override
    @CacheEvict(cacheNames = "TracePoint", key = "#workspaceId + '_' + #tracePointId")
    public void updateTracePoint(String workspaceId, String userId, String tracePointId, TracePoint tracePoint) {
        tracePointRepository.updateTracePoint(workspaceId, userId, tracePointId, tracePoint);
        if (tracePoint.isPredefined()) {
            tracePointExpireCountRepository.removeTracePointExpireCount(workspaceId, tracePointId);
        } else {
            tracePointExpireCountRepository.putTracePointExpireCount(
                    workspaceId, tracePointId, tracePoint.getExpireCount(), tracePoint.getExpireSecs());
            referenceEventService.removeReferenceEvent(workspaceId, tracePointId, ProbeType.TRACEPOINT);
        }
    }

    @Override
    public List<TracePoint> listTracePoints(String workspaceId, String userId) {
        return tracePointRepository.listTracePoints(workspaceId, userId);
    }

    @Override
    public Collection<TracePoint> queryTracePoints(String workspaceId, ApplicationFilter filter) {
        return tracePointRepository.queryTracePoints(workspaceId, filter);
    }

    @Override
    public CompletableFuture<Boolean> checkExpireAndDecrementTracePointExpireCount(
            String workspaceId, String tracePointId) {
        return tracePointExpireCountRepository.checkExpireAndDecrementTracePointExpireCount(workspaceId, tracePointId);
    }

    @Override
    public List<TracePoint> listPredefinedTracePoints(String workspaceId, String userId) {
        return tracePointRepository.listPredefinedTracePoints(workspaceId, userId);
    }

    @Override
    public TracePoint queryTracePoint(String workspaceId, String tracePointId, ApplicationFilter filter) {
        return tracePointRepository.queryTracePoint(workspaceId, tracePointId, filter);
    }

    @Override
    public List<TracePointConfig> queryTracePoints(
            String workspaceId, List<ApplicationFilter> applicationFilters, String tag) {
        List<TracePointConfig> filteredTracePoints = new ArrayList<>();
        List<TracePointConfig> tracePoints = tracePointRepository.queryTracePointsByTag(workspaceId, tag);
        applicationFilters.forEach(applicationFilter -> {
            for (TracePointConfig tracePoint : tracePoints) {
                if (tracePoint.getApplicationFilters().contains(applicationFilter)) {
                    filteredTracePoints.add(tracePoint);
                    break;
                }
            }
        });
        return filteredTracePoints;
    }
}
