package com.runsidekick.broker.repository;


import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ReferenceEventRepository {

    ReferenceEvent get(String probeId, ProbeType probeType);

    void save(ReferenceEvent referenceEvent);

    void delete(String probeId, ProbeType probeType);

    void delete(List<String> probeIds, ProbeType probeType);
}
