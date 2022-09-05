package com.runsidekick.broker.service.impl;

import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;
import com.runsidekick.broker.repository.ReferenceEventRepository;
import com.runsidekick.broker.service.ReferenceEventService;
import lombok.RequiredArgsConstructor;
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
    public ReferenceEvent getReferenceEvent(String workspaceId, String probeId, ProbeType probeType,
                                            ApplicationFilter applicationFilter) {
        return referenceEventRepository.get(workspaceId, probeId, probeType, applicationFilter);
    }

    @Override
    public void putReferenceEvent(ReferenceEvent referenceEvent) throws Exception {
        referenceEventRepository.save(referenceEvent);
    }

    @Override
    public void removeReferenceEvent(String workspaceId, String probeId, ProbeType probeType,
                                     ApplicationFilter applicationFilter) {
        referenceEventRepository.delete(workspaceId, probeId, probeType, applicationFilter);
    }

    @Override
    public void removeReferenceEvent(String workspaceId, String probeId, ProbeType probeType) {
        referenceEventRepository.delete(workspaceId, probeId, probeType);
    }

    @Override
    public void removeReferenceEvents(String workspaceId, List<String> probeIds, ProbeType probeType,
                                      ApplicationFilter applicationFilter) {
        referenceEventRepository.delete(workspaceId, probeIds, probeType, applicationFilter);
    }

    @Override
    public void removeReferenceEvents(String workspaceId, List<String> probeIds, ProbeType probeType) {
        referenceEventRepository.delete(workspaceId, probeIds, probeType);

    }
}
