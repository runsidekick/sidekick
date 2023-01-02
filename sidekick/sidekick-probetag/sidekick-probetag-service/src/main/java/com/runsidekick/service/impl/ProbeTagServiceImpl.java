package com.runsidekick.service.impl;

import com.runsidekick.model.ProbeTag;
import com.runsidekick.repository.ProbeTagRepository;
import com.runsidekick.service.ProbeTagService;
import io.thundra.swark.utils.UUIDUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yasin.kalafat
 */
@Service
public class ProbeTagServiceImpl implements ProbeTagService {

    private static final Logger LOGGER = LogManager.getLogger(ProbeTagServiceImpl.class);
    private static final int MAX_THREAD_COUNT = 10;

    @Autowired
    private ProbeTagRepository probeTagRepository;

    private ExecutorService executorService;

    @PostConstruct
    void initExecutor() {
        executorService = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
    }

    @Override
    @Cacheable(cacheNames = "ProbeTag", key = "#id")
    public ProbeTag get(String id) {
        return probeTagRepository.findById(id);
    }

    @Override
    public ProbeTag getByWorkspaceId(String workspaceId, String tag) {
        return probeTagRepository.getByWorkspaceId(workspaceId, tag);
    }

    @Override
    public List<ProbeTag> listByWorkspaceId(String workspaceId) {
        return probeTagRepository.listByWorkspaceId(workspaceId);
    }

    @Override
    @CacheEvict(cacheNames = "ProbeTag", key = "#probeTag.id")
    public ProbeTag add(ProbeTag probeTag) {
        probeTag.setId(UUID.randomUUID().toString());
        probeTagRepository.save(probeTag);
        return probeTag;
    }

    @Override
    public void add(String workspaceId, List<String> tags) {
        if (!CollectionUtils.isEmpty(tags)) {
            executorService.submit(() -> {
                try {
                    for (String tag : tags) {
                        probeTagRepository.save(ProbeTag.builder()
                                .id(UUIDUtils.generateId())
                                .tag(tag)
                                .workspaceId(workspaceId)
                                .build());
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            });
        }
    }


    @Override
    @CacheEvict(cacheNames = "ProbeTag", key = "#id")
    public void delete(String id) {
        probeTagRepository.delete(id);
    }

    @Override
    public void disableTag(String workspaceId, String tag) {
        probeTagRepository.disable(workspaceId, tag);
    }

    @Override
    public void enableTag(String workspaceId, String tag) {
        probeTagRepository.enable(workspaceId, tag);
    }
}
