package com.runsidekick.service;

import com.runsidekick.model.ProbeTag;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface ProbeTagService {

    List<ProbeTag> listByWorkspaceId(String workspaceId);

    ProbeTag add(ProbeTag probeTag);

    void add(String workspaceId, List<String> tags);

    ProbeTag get(String id);

    ProbeTag getByWorkspaceId(String workspaceId, String tag);

    void delete(String id);

    void disableTag(String workspaceId, String tag);

    void enableTag(String workspaceId, String tag);
}
