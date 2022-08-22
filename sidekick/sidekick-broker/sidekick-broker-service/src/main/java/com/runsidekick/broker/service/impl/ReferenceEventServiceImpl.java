package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.repository.ReferenceEventRepository;
import com.runsidekick.broker.service.ReferenceEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yasin.kalafat
 */
@RequiredArgsConstructor
@Service
public class ReferenceEventServiceImpl implements ReferenceEventService {

    private final ReferenceEventRepository referenceEventRepository;

    @Override
    @Cacheable(cacheNames = "ReferenceEvent", key = "#probeId + '_' + #probeType.name")
    public ReferenceEvent getReferenceEvent(String probeId, ProbeType probeType) {
        return referenceEventRepository.get(probeId, probeType);
    }

    @Override
    @CacheEvict(cacheNames = "ReferenceEvent", key = "#referenceEvent.probeId + '_' + #referenceEvent.probeType.name")
    public void putReferenceEvent(ReferenceEvent referenceEvent) {
        referenceEventRepository.save(referenceEvent);
    }

    @Override
    @CacheEvict(cacheNames = "ReferenceEvent", key = "#probeId + '_' + #probeType.name")
    public void removeReferenceEvent(String probeId, ProbeType probeType) {
        referenceEventRepository.delete(probeId, probeType);
    }

    @Override
    public void removeReferenceEvents(List<String> probeIds, ProbeType probeType) {
        referenceEventRepository.delete(probeIds, probeType);
    }
}
