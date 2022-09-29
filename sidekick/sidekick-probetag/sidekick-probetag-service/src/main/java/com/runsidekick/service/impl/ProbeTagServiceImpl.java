package com.runsidekick.service.impl;

import com.runsidekick.model.ProbeTag;
import com.runsidekick.repository.ProbeTagRepository;
import com.runsidekick.service.ProbeTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author yasin.kalafat
 */
@Service
public class ProbeTagServiceImpl implements ProbeTagService {

    @Autowired
    private ProbeTagRepository probeTagRepository;

    @Override
    @Cacheable(cacheNames = "ProbeTag", key = "#id")
    public ProbeTag get(String id) {
        return probeTagRepository.findById(id);
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
    @CacheEvict(cacheNames = "ProbeTag", key = "#id")
    public void delete(String id) {
        probeTagRepository.delete(id);
    }
}
