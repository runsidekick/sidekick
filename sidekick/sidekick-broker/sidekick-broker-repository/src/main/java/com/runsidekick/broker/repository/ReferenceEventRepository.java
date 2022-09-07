package com.runsidekick.broker.repository;


import com.runsidekick.broker.model.ApplicationFilter;
import com.runsidekick.broker.model.ProbeType;
import com.runsidekick.broker.model.ReferenceEvent;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ReferenceEventRepository {

    ReferenceEvent get(String workspaceId, String probeId, ProbeType probeType, ApplicationFilter applicationFilter);

    void save(ReferenceEvent referenceEvent) throws Exception;

    void delete(String workspaceId, String probeId, ProbeType probeType, ApplicationFilter applicationFilter);

    void delete(String workspaceId, String probeId, ProbeType probeType);

    void delete(String workspaceId, List<String> probeIds, ProbeType probeType, ApplicationFilter applicationFilter);

    void delete(String workspaceId, List<String> probeIds, ProbeType probeType);
}
