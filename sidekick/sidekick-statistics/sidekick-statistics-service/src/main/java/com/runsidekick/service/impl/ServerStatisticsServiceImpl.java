package com.runsidekick.service.impl;

import com.runsidekick.model.ServerStatistics;
import com.runsidekick.repository.ServerStatisticsRepository;
import com.runsidekick.service.ServerStatisticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yasin.kalafat
 */
@Service
public class ServerStatisticsServiceImpl implements ServerStatisticsService {

    private static final Logger LOGGER = LogManager.getLogger(ServerStatisticsServiceImpl.class);

    @Value("${phonehome.statistics.enabled:true}")
    private boolean phoneHomeStatisticsEnabled;

    @Value("${phonehome.statistics.threadcount:5}")
    private int phoneHomeStatisticsThreadCount;

    @Autowired
    private ServerStatisticsRepository serverStatisticsRepository;

    private ExecutorService executorService;

    @PostConstruct
    void initExecutor() {
        executorService = Executors.newFixedThreadPool(phoneHomeStatisticsThreadCount);
    }

    @Override
    public void increaseApplicationInstanceCount(String workspaceId) {
        if (phoneHomeStatisticsEnabled) {
            executorService.submit(() -> {
                try {
                    serverStatisticsRepository.add(workspaceId);
                    serverStatisticsRepository.increaseApplicationInstanceCount(workspaceId);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            });
        }
    }

    @Override
    public void increaseTracePointCount(String workspaceId) {
        if (phoneHomeStatisticsEnabled) {
            executorService.submit(() -> {
                try {
                    serverStatisticsRepository.add(workspaceId);
                    serverStatisticsRepository.increaseTracePointCount(workspaceId);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            });
        }
    }

    @Override
    public void increaseLogPointCount(String workspaceId) {
        if (phoneHomeStatisticsEnabled) {
            executorService.submit(() -> {
                try {
                    serverStatisticsRepository.add(workspaceId);
                    serverStatisticsRepository.increaseLogPointCount(workspaceId);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            });
        }
    }

    @Override
    public List<ServerStatistics> getAllServerStatistics() {
        return serverStatisticsRepository.getAllServerStatistics();
    }

    @Override
    public ServerStatistics getServerStatistics(String workspaceId) {
        return serverStatisticsRepository.getServerStatistics(workspaceId);
    }

}
