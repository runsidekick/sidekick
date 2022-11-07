package com.runsidekick.repository;

import com.runsidekick.model.EventHistory;
import com.runsidekick.model.request.EventHistoryRequest;

import java.util.List;

/**
 * @author yasin.kalafat
 */
public interface EventHistoryRepository {
    void save(EventHistory eventHistory);

    List<EventHistory> queryEventHistory(EventHistoryRequest request, int skip, int take);
}
