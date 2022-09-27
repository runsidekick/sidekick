package com.runsidekick.service.impl;

import com.runsidekick.model.ServerStatistics;
import com.runsidekick.repository.ServerStatisticsRepository;
import com.runsidekick.service.ServerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@Service
public class ServerStatisticsServiceImpl implements ServerStatisticsService {

    @Autowired
    private ServerStatisticsRepository serverStatisticsRepository;


    @Override
    @CacheEvict(cacheNames = "ServerStatistics", key = "#workspaceId")
    public void increaseApplicationInstanceCount(String workspaceId) {
        serverStatisticsRepository.add(workspaceId);
        serverStatisticsRepository.increaseApplicationInstanceCount(workspaceId);
    }

    @Override
    @CacheEvict(cacheNames = "ServerStatistics", key = "#workspaceId")
    public void increaseTracePointCount(String workspaceId) {
        serverStatisticsRepository.add(workspaceId);
        serverStatisticsRepository.increaseTracePointCount(workspaceId);
    }

    @Override
    @CacheEvict(cacheNames = "ServerStatistics", key = "#workspaceId")
    public void increaseLogPointCount(String workspaceId) {
        serverStatisticsRepository.add(workspaceId);
        serverStatisticsRepository.increaseLogPointCount(workspaceId);
    }

    @Override
    public List<ServerStatistics> getAllServerStatistics() {
        return serverStatisticsRepository.getAllServerStatistics();
    }

    @Override
    @Cacheable(cacheNames = "ServerStatistics", key = "#workspaceId")
    public ServerStatistics getServerStatistics(String workspaceId) {
        return serverStatisticsRepository.getServerStatistics(workspaceId);
    }

}
