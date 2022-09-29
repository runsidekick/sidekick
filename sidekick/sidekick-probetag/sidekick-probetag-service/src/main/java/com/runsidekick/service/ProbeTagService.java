package com.runsidekick.service;

import com.runsidekick.model.ProbeTag;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ProbeTagService {

    List<ProbeTag> listByWorkspaceId(String workspaceId);

    ProbeTag add(ProbeTag probeTag);

    ProbeTag get(String id);

    void delete(String id);
}
