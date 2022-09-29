package com.runsidekick.service.impl;

import com.runsidekick.model.PhoneHomeConfig;
import com.runsidekick.model.ServerStatistics;
import com.runsidekick.repository.ServerStatisticsRepository;
import com.runsidekick.service.ServerStatisticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PhoneHomeConfig phoneHomeConfig;

    @Autowired
    private ServerStatisticsRepository serverStatisticsRepository;

    private ExecutorService executorService;

    @PostConstruct
    void initExecutor() {
        executorService = Executors.newFixedThreadPool(phoneHomeConfig.getPhoneHomeStatisticsThreadCount());
    }

    @Override
    public void increaseApplicationInstanceCount(String workspaceId) {
        if (phoneHomeConfig.isPhoneHomeEnabled()
                && phoneHomeConfig.isPhoneHomeStatisticsEnabled()) {
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
        if (phoneHomeConfig.isPhoneHomeEnabled()
                && phoneHomeConfig.isPhoneHomeStatisticsEnabled()) {
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
        if (phoneHomeConfig.isPhoneHomeEnabled()
                && phoneHomeConfig.isPhoneHomeStatisticsEnabled()) {
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
