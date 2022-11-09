package com.runsidekick.service;

import com.runsidekick.broker.model.LogPoint;
import com.runsidekick.broker.model.TracePoint;
import com.runsidekick.broker.model.event.impl.ErrorStackSnapshotEvent;
import com.runsidekick.broker.model.event.impl.LogPointEvent;
import com.runsidekick.broker.model.event.impl.TracePointSnapshotEvent;
import com.runsidekick.model.EventHistory;
import com.runsidekick.model.EventHitCount;
import com.runsidekick.model.request.EventHistoryRequest;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface EventHistoryService {

    void addTracePointEventHistory(String workspaceId, TracePointSnapshotEvent event, TracePoint tracePoint,
                                   String rawMessage);

    void addLogPointEventHistory(String workspaceId, LogPointEvent event, LogPoint logPoint, String rawMessage);

    void addErrorSnapshotEventHistory(String workspaceId, ErrorStackSnapshotEvent event, String rawMessage);

    List<EventHistory> queryEventHistory(EventHistoryRequest request, int page, int size);

    List<EventHitCount> getCountsGroupedByDate(EventHistoryRequest request);
}
