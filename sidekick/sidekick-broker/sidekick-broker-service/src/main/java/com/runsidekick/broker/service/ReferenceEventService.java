package com.runsidekick.broker.service;


import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ReferenceEventService {

    ReferenceEvent getReferenceEvent(String workspaceId, String probeId, ProbeType probeType,
                                     ApplicationFilter applicationFilter);

    void putReferenceEvent(ReferenceEvent referenceEvent) throws Exception;

    void removeReferenceEvent(String workspaceId, String probeId, ProbeType probeType,
                              ApplicationFilter applicationFilter);

    void removeReferenceEvent(String workspaceId, String probeId, ProbeType probeType);

    void removeReferenceEvents(String workspaceId, List<String> probeIds, ProbeType probeType,
                               ApplicationFilter applicationFilter);

    void removeReferenceEvents(String workspaceId, List<String> probeIds, ProbeType probeType);
}
