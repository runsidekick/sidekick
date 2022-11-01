package com.runsidekick.repository;

import com.runsidekick.model.ProbeTag;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ProbeTagRepository {

    ProbeTag findById(String id);

    List<ProbeTag> listByWorkspaceId(String workspaceId);

    void save(ProbeTag probeTag);

    void delete(String id);

    void disable(String workspaceId, String tag);

    void enable(String workspaceId, String tag);
}
