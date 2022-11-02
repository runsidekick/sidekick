package com.runsidekick.service;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.event.impl.ErrorStackSnapshotEvent;
import com.runsidekick.broker.model.event.impl.LogPointEvent;
import com.runsidekick.broker.model.event.impl.TracePointSnapshotEvent;

/**
 * @author yasin.kalafat
 */
public interface EventHistoryService {

    void addTracePointEventHistory(String workspaceId, TracePointSnapshotEvent event, TracePoint tracePoint,
                                   String rawMessage);

    void addLogPointEventHistory(String workspaceId, LogPointEvent event, LogPoint logPoint, String rawMessage);

    void addErrorSnapshotEventHistory(String workspaceId, ErrorStackSnapshotEvent event, String rawMessage);
}
