package com.runsidekick.broker.service;


import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ReferenceEventService {

    ReferenceEvent getReferenceEvent(String probeId, ProbeType eventType);

    void putReferenceEvent(ReferenceEvent referenceEvent);

    void removeReferenceEvent(String probeId, ProbeType eventType);

    void removeReferenceEvents(List<String> probeIds, ProbeType probeType);
}
